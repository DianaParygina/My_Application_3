package com.example.myapplication_3

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IncomeActivity : BaseMenu() {

    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
    private lateinit var recyclerView: RecyclerView
    val sharedPrefs by lazy { getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

//        // Загрузка списка доходов из SharedPreferences
//        val incomeString = sharedPrefs.getString("incomeList", "")
//        val incomeItems = incomeString?.split(",")?.toMutableList() ?: mutableListOf()

        incomeAdapter = IncomeAdapter(mutableListOf("0"), sharedFinanceViewModel,this)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
                    val updatedIncome = incomeAdapter.getIncomesList()
                    val incomeString = updatedIncome.joinToString(",")
                    with (sharedPrefs.edit()) {
                        putString("incomeList", incomeString)
                        apply()
                    }

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

    override fun onResume() {
        super.onResume()

        // Загрузка списка доходов из SharedPreferences
        val incomeString = sharedPrefs.getString("incomeList", "")
        val incomeItems = incomeString?.split(",")?.toMutableList() ?: mutableListOf()

        // Очистите текущий список в адаптере и добавьте новые элементы
        incomeAdapter.incomeItems.clear()
        incomeAdapter.incomeItems.addAll(incomeItems)
        incomeAdapter.notifyDataSetChanged()
    }
}