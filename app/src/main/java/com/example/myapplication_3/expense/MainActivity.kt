package com.example.myapplication_3.expense
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.myapplication_3.BaseMenu
import com.example.myapplication_3.MyApplication
import com.example.myapplication_3.R
import com.example.myapplication_3.SharedFinanceViewModel
import com.example.myapplication_3.income.BinFileHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : BaseMenu() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView
    private var selectedPositionForContextMenu: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        registerForContextMenu(findViewById(android.R.id.content))

        expenseAdapter = ExpenseAdapter(mutableListOf(), sharedFinanceViewModel, this)
        recyclerView = findViewById(R.id.recyclerView)

        expenseAdapter.expenseItems.addAll(XLSFileHandler.loadDataFromXLS())
        expenseAdapter.notifyDataSetChanged()

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = expenseAdapter


        val buttonPdfExpense = findViewById<Button>(R.id.button_pdf_expense)
        buttonPdfExpense.setOnClickListener {
            val expenses = expenseAdapter.expenseItems // Получите список расходов из адаптера
            PDFGeneratorExpense.generatePdf(this, expenses)
            Toast.makeText(this, "PDF отчет по расходам создан", Toast.LENGTH_SHORT).show()
        }

        val buttonPdfExpenseOpen = findViewById<Button>(R.id.button_pdf_expense_open)
        buttonPdfExpenseOpen.setOnClickListener {
            val pdfPathExpense = PDFGeneratorExpense.getPdfFilePath(this)
            if (pdfPathExpense != null) {
                val uriExpense = FileProvider.getUriForFile(this, "${packageName}.provider", File(pdfPathExpense))
                val intentExpense = Intent(Intent.ACTION_VIEW)
                intentExpense.setDataAndType(uriExpense, "application/pdf")
                intentExpense.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(Intent.createChooser(intentExpense, "Открыть PDF"))
            }else {
                Toast.makeText(this, "Ошибка: PDF файл не найден", Toast.LENGTH_SHORT).show()
            }
        }



        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        updateBottomNavigationView(R.id.Main)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.context_menu_main, menu)
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

    fun showContextMenuForItem(position: Int, view: View) {
        registerForContextMenu(view)
        view.showContextMenu()
        unregisterForContextMenu(view)
        selectedPositionForContextMenu = position
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить расход")
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        builder.setView(view)

        val inputExpense = view.findViewById<EditText>(R.id.input_expense)
        val inputDate = view.findViewById<EditText>(R.id.input_date_expense)
        val inputType = view.findViewById<EditText>(R.id.input_type_expense)

        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            inputDate.setText(format.format(calendar.time))
        }

        inputDate.setOnClickListener {
            DatePickerDialog(this, datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        builder.setPositiveButton("OK") { _, _ ->
            val expenseString = inputExpense.text.toString()
            val dateString = inputDate.text.toString()
            val typeString = inputType.text.toString()
            if (expenseString.isNotEmpty()) {
                val expense = expenseString.toDoubleOrNull()
                if (expense != null) {
                    val expenseItem = ExpenseItem(expense, dateString, typeString)
                    expenseAdapter.addExpense(expenseItem)
                    expenseAdapter.notifyItemInserted(0)



                    XLSFileHandler.addLineToXLS(expenseItem)
                    sharedFinanceViewModel.addExpense(expense)

                    showToast("Ваш расход ${sharedFinanceViewModel.getTotalExpense()} руб")
                } else {
                    showToast("Введите корректное число")
                }
            } else {
                showToast("Введите сумму расхода")
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onResume() {
        super.onResume()
        expenseAdapter.expenseItems.clear() // Очищаем список перед загрузкой
        val expenseItemsFromFile = XLSFileHandler.loadDataFromXLS()
        expenseAdapter.expenseItems.addAll(expenseItemsFromFile)
        expenseAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()

        val dataToStore = expenseAdapter.expenseItems.map { expenseItem ->
            "${expenseItem.expense},${expenseItem.date},${expenseItem.type}"
        }

        XLSFileHandler.saveDataToXLS(dataToStore)
    }
}