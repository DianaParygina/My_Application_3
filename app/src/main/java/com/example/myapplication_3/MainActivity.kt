package com.example.myapplication_3
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem

class MainActivity : BaseMenu() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView
    val sharedPrefs by lazy { getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        expenseAdapter = ExpenseAdapter(mutableListOf("0"), sharedFinanceViewModel, this, sharedPrefs)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = expenseAdapter
        registerForContextMenu(recyclerView)

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

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить расход")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val expenseString = input.text.toString()
            if (expenseString.isNotEmpty()) {
                val expense = expenseString.toDoubleOrNull()
                if (expense != null) {
                    sharedFinanceViewModel.addExpense(expense)
                    expenseAdapter.addExpense(expense.toString() + "руб")
                    val updatedExpenses = expenseAdapter.getExpenseList()
                    val expensesString = updatedExpenses.joinToString(",")
                    with (sharedPrefs.edit()) {
                        putString("expenseList", expensesString)
                        apply()
                    }

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
        val expensesString = sharedPrefs.getString("expenseList", "")
        val expenseItems = expensesString?.split(",")?.toMutableList() ?: mutableListOf()

        // Очистите текущий список в адаптере и добавьте новые элементы
        expenseAdapter.expenseItems.clear()
        expenseAdapter.expenseItems.addAll(expenseItems)
        expenseAdapter.notifyDataSetChanged()
    }
}