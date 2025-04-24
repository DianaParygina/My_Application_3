package com.example.myapplication_3.Frameworks.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.example.myapplication_3.Frameworks.database.IncomeDatabaseHelper
import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.Frameworks.files.BinFileHandler
import com.example.myapplication_3.Frameworks.files.PDFGeneratorIncome
import com.example.myapplication_3.R
import com.example.myapplication_3.Controllers.SharedFinanceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class IncomeFragment : Fragment() {
    private lateinit var incomeAdapter: IncomeAdapter
    private val sharedFinanceViewModel: SharedFinanceViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_income, container, false)

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
        recyclerView = view.findViewById(R.id.recyclerViewIncome)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = incomeAdapter


        // Кнопки для переключения режима SQL
        val buttonSql = view.findViewById<Button>(R.id.button_sql)
        buttonSql.setOnClickListener {
            if (!currentModeIsSql) {
                useSql = true
                saveUseSqlState()
                loadIncomes()
                currentModeIsSql = true
                Toast.makeText(requireContext(), "Работа с SQL включена", Toast.LENGTH_SHORT).show()
            }
        }

        val buttonSqlDontUse = view.findViewById<Button>(R.id.button_sql_dont_use)
        buttonSqlDontUse.setOnClickListener {
            if (currentModeIsSql) {
                useSql = false
                saveUseSqlState()
                loadIncomes()
                currentModeIsSql = false
                Toast.makeText(requireContext(), "Работа с SQL выключена", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка для генерации PDF
        val buttonPdfIncome = view.findViewById<Button>(R.id.button_pdf_income)
        buttonPdfIncome.setOnClickListener {
            val incomes = incomeAdapter.incomes
            PDFGeneratorIncome.generatePdf(requireContext(), incomes)
            val pdfPathIncome = PDFGeneratorIncome.getPdfFilePath(requireContext())
            if (pdfPathIncome != null) {
                val uriIncome = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.provider",
                    File(pdfPathIncome)
                )
                val intentIncome = Intent(Intent.ACTION_VIEW)
                intentIncome.setDataAndType(uriIncome, "application/pdf")
                intentIncome.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(Intent.createChooser(intentIncome, "Открыть PDF"))
            }
        }


        // Настройка PagerSnapHelper для постраничного скроллинга
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // Настройка ItemTouchHelper для свайпов
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (direction == ItemTouchHelper.DOWN) {
                    incomeAdapter.deleteIncome(position)
                } else if (direction == ItemTouchHelper.UP) {
                    incomeAdapter.showEditIncomeDialog(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Загрузка данных
        loadIncomes()

        // Регистрация контекстного меню
        registerForContextMenu(recyclerView)

        return view
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        loadIncomes()
//
//        if (incomeAdapter.incomes.isEmpty()) {
//            val testIncome = Income(null, 100.0, "01.01.2024", "Зарплата")
//            incomeAdapter.addIncome(testIncome)
//            BinFileHandler.addLineToBin(testIncome)
//            incomeAdapter.notifyDataSetChanged()
//        }
//    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
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
//        registerForContextMenu(view) // Регистрируем перед показом
        view.showContextMenu()
        unregisterForContextMenu(view) // Отменяем регистрацию после показа
    }

    private fun showAddIncomeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Добавить доход")
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null)
        builder.setView(view)

        val inputIncome = view.findViewById<EditText>(R.id.input_amount)
        val inputDate = view.findViewById<EditText>(R.id.input_date)

        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            inputDate.setText(format.format(calendar.time))
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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            incomeTypes.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        builder.setPositiveButton("OK") { _, _ ->
            val incomeString = inputIncome.text.toString()
            val dateString = inputDate.text.toString()
            if (incomeString.isNotEmpty() && dateString.isNotEmpty()) {
                val incomeAmount = incomeString.toDoubleOrNull()
                if (incomeAmount != null) {
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
                } else {
                    showToast("Введите корректную сумму")
                }
            } else {
                showToast("Заполните все поля")
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showAddTypeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Добавить тип дохода")
        val input = EditText(requireContext())
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val typeName = input.text.toString().trim()
            if (typeName.isNotEmpty()) {
                dbHelper.insertIncomeType(dbHelper.writableDatabase, typeName)
                loadIncomes()
                showToast("Тип дохода добавлен")
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
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
}