package com.example.myapplication_3

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem


class MainActivity : BaseMenu() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        expenseAdapter = ExpenseAdapter(mutableListOf("0"))
        recyclerView = findViewById(R.id.recyclerView)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
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

    private fun showAddExpenseDialog(){
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
}