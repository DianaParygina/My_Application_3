package com.example.myapplication_3.Frameworks.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.Frameworks.database.IncomeDatabaseHelper
import com.example.myapplication_3.Frameworks.files.BinFileHandler
import com.example.myapplication_3.Frameworks.files.PDFGeneratorIncome
import com.example.myapplication_3.Controllers.SharedFinanceViewModel
import com.example.myapplication_3.R
import com.example.myapplication_3.databinding.FragmentIncomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class IncomeFragment : Fragment() {
    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeAdapter: IncomeAdapter
    private val sharedFinanceViewModel: SharedFinanceViewModel by viewModels()
    private lateinit var dbHelper: IncomeDatabaseHelper
    var useSql = false
    private lateinit var sharedPreferences: SharedPreferences
    private val USE_SQL_KEY = "use_sql"
    private var currentModeIsSql = false
    private var selectedPositionForContextMenu: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация SharedPreferences
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        useSql = sharedPreferences.getBoolean(USE_SQL_KEY, false)
        currentModeIsSql = useSql

        // Инициализация базы данных
        dbHelper = IncomeDatabaseHelper(requireContext())

        // Инициализация бинарного файла
        BinFileHandler.initialize(requireContext(), "incomes.bin")

        // Настройка RecyclerView
        incomeAdapter = IncomeAdapter(ArrayList(), sharedFinanceViewModel, this, dbHelper)
        binding.recyclerViewIncome.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = incomeAdapter
        }

        // Кнопки для переключения режима SQL
        binding.buttonSql.setOnClickListener {
            if (!currentModeIsSql) {
                useSql = true
                saveUseSqlState()
                loadIncomes()
                currentModeIsSql = true
                showToast("Работа с SQL включена")
            }
        }

        binding.buttonSqlDontUse.setOnClickListener {
            if (currentModeIsSql) {
                useSql = false
                saveUseSqlState()
                loadIncomes()
                currentModeIsSql = false
                showToast("Работа с SQL выключена")
            }
        }

        // Кнопка для генерации PDF
        binding.buttonPdfIncome.setOnClickListener {
            val incomes = incomeAdapter.incomes
            PDFGeneratorIncome.generatePdf(requireContext(), incomes)
            PDFGeneratorIncome.getPdfFilePath(requireContext())?.let { pdfPathIncome ->
                val uriIncome = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.provider",
                    File(pdfPathIncome)
                )
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uriIncome, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(Intent.createChooser(this, "Открыть PDF"))
                }
            }
        }

        // Настройка PagerSnapHelper для постраничного скроллинга
        PagerSnapHelper().attachToRecyclerView(binding.recyclerViewIncome)

        // Настройка ItemTouchHelper для свайпов
        ItemTouchHelper(createItemTouchHelperCallback()).attachToRecyclerView(binding.recyclerViewIncome)

        // Загрузка данных
        loadIncomes()

        // Регистрация контекстного меню
        registerForContextMenu(binding.recyclerViewIncome)
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                when (direction) {
                    ItemTouchHelper.DOWN -> incomeAdapter.deleteIncome(position)
                    ItemTouchHelper.UP -> incomeAdapter.showEditIncomeDialog(position)
                }
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.context_menu_income, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_income -> {
                showAddIncomeDialog()
                true
            }
            R.id.add_income_type -> {
                showAddTypeDialog()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    fun showContextMenuForItem(position: Int, view: View) {
        selectedPositionForContextMenu = position
        view.showContextMenu()
        unregisterForContextMenu(view)
    }

    private fun showAddIncomeDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Добавить доход")
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null)
            setView(view)

            val inputIncome = view.findViewById<EditText>(R.id.input_amount)
            val inputDate = view.findViewById<EditText>(R.id.input_date)

            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                inputDate.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time))
            }

            inputDate.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    datePicker,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            val spinner = view.findViewById<Spinner>(R.id.income_type_spinner)
            val incomeTypes = dbHelper.getAllIncomeTypes()

            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                incomeTypes.map { it.name }
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            setPositiveButton("OK") { _, _ ->
                val incomeString = inputIncome.text.toString()
                val dateString = inputDate.text.toString()
                if (incomeString.isNotEmpty() && dateString.isNotEmpty()) {
                    incomeString.toDoubleOrNull()?.let { incomeAmount ->
                        val selectedType = incomeTypes[spinner.selectedItemPosition]

                        val newIncome = if (useSql) {
                            val newIncomeId = dbHelper.insertIncome(incomeAmount, dateString, selectedType.id.toLong())
                            Income(newIncomeId, incomeAmount, dateString, selectedType.name)
                        } else {
                            Income(null, incomeAmount, dateString, selectedType.name)
                        }

                        incomeAdapter.addIncome(newIncome)
                        showToast("Доход добавлен")
                        sharedFinanceViewModel.addIncome(incomeAmount, dateString, selectedType.name)
                    } ?: showToast("Введите корректную сумму")
                } else {
                    showToast("Заполните все поля")
                }
            }

            setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun showAddTypeDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Добавить тип дохода")
            val input = EditText(requireContext())
            setView(input)

            setPositiveButton("OK") { _, _ ->
                input.text.toString().trim().takeIf { it.isNotEmpty() }?.let { typeName ->
                    dbHelper.insertIncomeType(dbHelper.writableDatabase, typeName)
                    loadIncomes()
                    showToast("Тип дохода добавлен")
                }
            }

            setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun loadIncomes() {
        incomeAdapter.incomes.clear()
        incomeAdapter.incomes.addAll(if (useSql) dbHelper.getAllIncomes() else BinFileHandler.loadDataFromBin())
        incomeAdapter.notifyDataSetChanged()
    }

    private fun saveUseSqlState() {
        sharedPreferences.edit().putBoolean(USE_SQL_KEY, useSql).apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if (!useSql) {
            BinFileHandler.saveDataToBin(incomeAdapter.incomes)
        }
    }

    override fun onResume() {
        super.onResume()
        loadIncomes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}