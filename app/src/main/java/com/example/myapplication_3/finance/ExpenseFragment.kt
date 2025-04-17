package com.example.myapplication_3.expense

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication_3.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExpenseFragment : Fragment() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView
    private var selectedMenuItemPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense, container, false)
        XLSFileHandler.initialize(requireContext(), "expenses.xls")
        sharedFinanceViewModel = (requireActivity().application as MyApplication).sharedFinanceViewModel

        expenseAdapter = ExpenseAdapter(mutableListOf(), sharedFinanceViewModel, this)
        recyclerView = view.findViewById(R.id.recyclerViewExpenses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = expenseAdapter

        view.findViewById<Button>(R.id.button_pdf_expense).setOnClickListener {
            PDFGeneratorExpense.generatePdf(requireContext(), expenseAdapter.expenseItems)
            showToast("PDF отчет по расходам создан")
        }

        view.findViewById<Button>(R.id.button_pdf_expense_open).setOnClickListener {
            PDFGeneratorExpense.getPdfFilePath(requireContext())?.let { pdfPath ->
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.provider",
                    File(pdfPath)
                )
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(Intent.createChooser(this, "Открыть PDF"))
                }
            } ?: showToast("Ошибка: PDF файл не найден")
        }

        PagerSnapHelper().attachToRecyclerView(recyclerView)

        ItemTouchHelper(createItemTouchHelperCallback()).attachToRecyclerView(recyclerView)

        registerForContextMenu(recyclerView)

        return view
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
                    ItemTouchHelper.DOWN -> expenseAdapter.deleteExpense(position)
                    ItemTouchHelper.UP -> expenseAdapter.showEditExpenseDialog(position)
                }
            }
        }
    }

    private fun loadExpenses() {
        expenseAdapter.expenseItems.clear()
        expenseAdapter.expenseItems.addAll(XLSFileHandler.loadDataFromXLS())
        expenseAdapter.notifyDataSetChanged()
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        loadExpenses()
//
//        if (expenseAdapter.expenseItems.isEmpty()) {
//            val testExpense = ExpenseItem(100.0, "01.01.2024", "Зарплата")
//            expenseAdapter.addExpense(testExpense)
//            XLSFileHandler.addLineToXLS(testExpense)
//            expenseAdapter.notifyDataSetChanged()
//        }
//    }


    fun showContextMenuForItem(position: Int, view: View) {
        selectedMenuItemPosition = position
        view.showContextMenu()
        unregisterForContextMenu(view)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.context_menu_main, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_expense -> {
                showAddExpenseDialog()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("Добавить расход")
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
            setView(view)

            val inputExpense = view.findViewById<EditText>(R.id.input_expense)
            val inputDate = view.findViewById<EditText>(R.id.input_date_expense)
            val inputType = view.findViewById<EditText>(R.id.input_type_expense)

            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
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

            setPositiveButton("OK") { _, _ ->
                val expense = inputExpense.text.toString().toDoubleOrNull()
                val date = inputDate.text.toString()
                val type = inputType.text.toString()

                if (expense != null && date.isNotEmpty() && type.isNotEmpty()) {
                    ExpenseItem(expense, date, type).let { item ->
                        expenseAdapter.addExpense(item)
                        XLSFileHandler.addLineToXLS(item)
                        sharedFinanceViewModel.addExpense(expense)
                        showToast("Ваш расход ${sharedFinanceViewModel.getTotalExpense()} руб")
                    }
                } else {
                    showToast("Заполните все поля корректно")
                }
            }
            setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    override fun onPause() {
        super.onPause()
        XLSFileHandler.saveDataToXLS(expenseAdapter.expenseItems.map {
            "${it.expense},${it.date},${it.type}"
        })
    }
}