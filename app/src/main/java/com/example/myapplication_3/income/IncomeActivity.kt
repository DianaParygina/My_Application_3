package com.example.myapplication_3.income

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.BD.IncomeDatabaseHelper
import com.example.myapplication_3.BaseMenu
import com.example.myapplication_3.MyApplication
import com.example.myapplication_3.R
import com.example.myapplication_3.SharedFinanceViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IncomeActivity : BaseMenu() {

    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView
    private var selectedPositionForContextMenu: Int = -1
    private var useSql = false
    private lateinit var dbHelper: IncomeDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private val USE_SQL_KEY = "use_sql"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        useSql = sharedPreferences.getBoolean(USE_SQL_KEY, false)

        dbHelper = IncomeDatabaseHelper(this)

        val buttonSql = findViewById<Button>(R.id.button_sql)
        buttonSql.setOnClickListener {
            useSql = true
            saveUseSqlState()
            loadIncomes() // Перезагружаем данные из выбранного источника
            // ...  (возможно, нужно обновить адаптер или другие элементы UI)
            Toast.makeText(this, "Работа с SQL включена", Toast.LENGTH_SHORT).show()


        }

        val buttonSqlDontUse = findViewById<Button>(R.id.button_sql_dont_use)
        buttonSqlDontUse.setOnClickListener {
            useSql = false
            saveUseSqlState()
            loadIncomes()
//            dbHelper.clearIncomesTable()// Перезагружаем данные из выбранного источника
            // ... (возможно, нужно обновить адаптер или другие элементы UI)
            Toast.makeText(this, "Работа с SQL выключена", Toast.LENGTH_SHORT).show()

        }

        registerForContextMenu(findViewById(android.R.id.content))

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel
        BinFileHandler.initialize(this, "incomes.bin")

        incomeAdapter = IncomeAdapter(mutableListOf(), sharedFinanceViewModel, this)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = incomeAdapter

//        val incomeItems = BinFileHandler.loadDataFromBin()
//        incomeAdapter.incomeItems.addAll(incomeItems)
        incomeAdapter.notifyDataSetChanged()

        val buttonPdfIncome = findViewById<Button>(R.id.button_pdf_income)
        buttonPdfIncome.setOnClickListener {
            val incomes = incomeAdapter.incomeItems
            PDFGeneratorIncome.generatePdf(this, incomes)
            Toast.makeText(this, "PDF отчет по доходам создан", Toast.LENGTH_SHORT).show()
        }

        val buttonPdfIncomeOpen = findViewById<Button>(R.id.button_pdf_income_open)
        buttonPdfIncomeOpen.setOnClickListener {
            val pdfPathIncome = PDFGeneratorIncome.getPdfFilePath(this)
            if (pdfPathIncome != null) {
                val uriIncome = FileProvider.getUriForFile(this, "${packageName}.provider", File(pdfPathIncome))
                val intentIncome = Intent(Intent.ACTION_VIEW)
                intentIncome.setDataAndType(uriIncome, "application/pdf")
                intentIncome.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(Intent.createChooser(intentIncome, "Открыть PDF"))
            }
        }




        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        updateBottomNavigationView(R.id.Income)

        loadIncomes()
    }


    override fun getLayoutResId(): Int {
        return R.layout.activity_income
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.context_menu_income, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_income -> {
                showAddIncomeDialog()
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

    private fun showAddIncomeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить доход")
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_income, null)
        builder.setView(view)

        val inputIncome = view.findViewById<EditText>(R.id.input_amount)
        val inputDate = view.findViewById<EditText>(R.id.input_date)
        val inputType = view.findViewById<EditText>(R.id.input_type)

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
                this,
                datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        builder.setPositiveButton("OK") { _, _ ->
            val incomeString = inputIncome.text.toString()
            val dateString = inputDate.text.toString()
            val typeString = inputType.text.toString()

            if (incomeString.isNotEmpty() && dateString.isNotEmpty() && typeString.isNotEmpty()) {
                val incomeAmount = incomeString.toDoubleOrNull()

                if (incomeAmount != null) {
                    if (useSql) {
                        val newIncomeId = dbHelper.insertIncome(incomeAmount, dateString, typeString)
                        if (newIncomeId != -1L) {
                            val newIncome = dbHelper.getIncomeById(newIncomeId)
                            if (newIncome != null) {
                                incomeAdapter.incomeItems.add(
                                    0,
                                    IncomeItem(newIncome.amount, newIncome.date, newIncome.typeId.toString())
                                )
                                incomeAdapter.notifyItemInserted(0)
                                showToast("Доход добавлен в базу данных") // Уведомление об успешном добавлении
                            } else {
                                showToast("Ошибка при добавлении дохода в базу данных")
                            }
                        } else {
                            showToast("Ошибка при добавлении дохода в базу данных")
                        }
                        sharedFinanceViewModel.addIncome(incomeAmount) // Обновление ViewModel

                    } else {
                        val incomeItem = IncomeItem(incomeAmount, dateString, typeString)
                        BinFileHandler.addLineToBin(incomeItem)
                        sharedFinanceViewModel.addIncome(incomeAmount)
                        incomeAdapter.incomeItems.add(0, incomeItem)
                        incomeAdapter.notifyItemInserted(0)
                        showToast("Ваш доход ${sharedFinanceViewModel.getTotalIncome()} руб")
                    }
                } else {
                    showToast("Введите корректные данные")
                }
            } else {
                showToast("Заполните все поля")
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }



    override fun onResume() {
        super.onResume()
        loadIncomes()

//        incomeAdapter.incomeItems.clear()
//        incomeAdapter.incomeItems.addAll(BinFileHandler.loadDataFromBin())
//        incomeAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        if (!useSql) { // Сохраняем в файл, только если не используем SQL
            BinFileHandler.saveDataToBin(incomeAdapter.incomeItems)
        }
    }


    private fun loadIncomes() {
        incomeAdapter.incomeItems.clear()
        if (useSql) {
            val incomesFromDb = dbHelper.getAllIncomes()
            val incomeItems = incomesFromDb.map { dbIncome ->
                IncomeItem(dbIncome.amount, dbIncome.date, dbIncome.typeId.toString())
            }
            incomeAdapter.incomeItems.addAll(incomeItems)
        } else {
            incomeAdapter.incomeItems.addAll(BinFileHandler.loadDataFromBin())
        }
        incomeAdapter.notifyDataSetChanged()
    }

    private fun saveUseSqlState() {
        with(sharedPreferences.edit()) {
            putBoolean(USE_SQL_KEY, useSql)
            apply()
        }
    }
}