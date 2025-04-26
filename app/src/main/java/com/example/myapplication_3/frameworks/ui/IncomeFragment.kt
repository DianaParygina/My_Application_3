package com.example.myapplication_3.frameworks.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.entities.Income
import com.example.myapplication_3.frameworks.database.IncomeDatabaseHelper
import com.example.myapplication_3.frameworks.files.BinFileHandler
import com.example.myapplication_3.frameworks.files.PDFGeneratorIncome
import com.example.myapplication_3.controllers.SharedFinanceViewModel
import com.example.myapplication_3.R
import com.example.myapplication_3.databinding.FragmentIncomeBinding
import dagger.hilt.android.AndroidEntryPoint
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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = sharedFinanceViewModel
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        useSql = sharedPreferences.getBoolean(USE_SQL_KEY, false)
        currentModeIsSql = useSql
        dbHelper = IncomeDatabaseHelper(requireContext())
        BinFileHandler.initialize(requireContext(), "incomes.bin")
        incomeAdapter = IncomeAdapter(ArrayList(), sharedFinanceViewModel, this, dbHelper)
        binding.recyclerViewIncome.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = incomeAdapter
        }

        PagerSnapHelper().attachToRecyclerView(binding.recyclerViewIncome)
        ItemTouchHelper(createItemTouchHelperCallback()).attachToRecyclerView(binding.recyclerViewIncome)
        registerForContextMenu(binding.recyclerViewIncome)

        loadIncomes()
    }

    fun enableSqlMode() {
        if (!currentModeIsSql) {
            useSql = true
            saveUseSqlState()
            loadIncomes()
            currentModeIsSql = true
            showToast("Работа с SQL включена")
        }
    }

    fun disableSqlMode() {
        if (currentModeIsSql) {
            useSql = false
            saveUseSqlState()
            loadIncomes()
            currentModeIsSql = false
            showToast("Работа с SQL выключена")
        }
    }

    fun generatePdfReport() {
        PDFGeneratorIncome.generateAndShowPdf(requireContext(), incomeAdapter.incomes)
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

            inputDate.setOnClickListener { DatePickerDialog(requireContext(), datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }

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