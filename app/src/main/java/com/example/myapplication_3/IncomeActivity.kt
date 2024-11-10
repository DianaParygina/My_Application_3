package com.example.myapplication_3

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IncomeActivity : BaseMenu() {

    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        incomeAdapter = IncomeAdapter(mutableListOf("0"))
        recyclerView = findViewById(R.id.recyclerView)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = incomeAdapter
        registerForContextMenu(recyclerView)

        updateBottomNavigationView(R.id.Income)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_income
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
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

    private fun showAddIncomeDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить доход")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val incomeString = input.text.toString()
            if (incomeString.isNotEmpty()) {
                val income = incomeString.toDoubleOrNull()
                if (income!= null) {
                    sharedFinanceViewModel.addIncome(income)
                    incomeAdapter.addIncome(income.toString() + "руб")
                    showToast("Ваш расход ${sharedFinanceViewModel.getTotalIncome()} руб")
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