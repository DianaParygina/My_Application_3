package com.example.myapplication_3
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : BaseMenu() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView
    val sharedPrefs by lazy { getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) }
    private var selectedPositionForContextMenu: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        expenseAdapter = ExpenseAdapter(mutableListOf(ExpenseItem(.0, "", "")), sharedFinanceViewModel, this, sharedPrefs)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = expenseAdapter

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
                    sharedFinanceViewModel.addExpense(expense)
                    expenseAdapter.addExpense(expenseItem)

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

        // Загрузка списка расходов из SharedPreferences
        val expenseString = sharedPrefs.getString("expenseList", "")?.split(";")?:emptyList()
        val expenseItems = expenseString.mapNotNull {
            val parts = it.split(",")
            if (parts.size == 3) {
                val expense = parts[0].toDoubleOrNull()
                val date = parts[1]
                val type = parts[2]
                if (expense != null) ExpenseItem(expense, date, type) else null
            } else null
        }

        if (expenseItems.isNotEmpty()) {
            expenseAdapter.expenseItems.removeAt(0)
        }

        expenseAdapter.expenseItems.addAll(expenseItems)
        expenseAdapter.notifyDataSetChanged()
    }
}