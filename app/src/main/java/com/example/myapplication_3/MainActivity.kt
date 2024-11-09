package com.example.myapplication_3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : BaseMenu() {

    private lateinit var editTextAddition: EditText
    private lateinit var buttonExpence: Button
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editTextAddition = findViewById(R.id.editTextAddition)
        buttonExpence = findViewById(R.id.buttonExpence)
        expenseAdapter = ExpenseAdapter(mutableListOf())

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        val expenseItem = ExpenseAdapter(mutableListOf())

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseItem

        buttonExpence.setOnClickListener {
            val expenceString = editTextAddition.text.toString()
            if (expenceString.isNotEmpty()) {
                val expence = expenceString.toDoubleOrNull()
                if (expence != null) {
                    sharedFinanceViewModel.addExpense(expence)
                        expenseAdapter.addExpense(expence.toString() + "руб")
                        recyclerView.adapter?.notifyItemInserted(expenseAdapter.itemCount - 1) // Обновляем адаптер
                        showToast("Ваш расход ${sharedFinanceViewModel.getTotalExpense()} руб")
                    editTextAddition.text.clear()
                } else {
                    showToast("Введите корректное число")
                }
            } else {
                showToast("Введите сумму расхода")
            }
        }
        updateBottomNavigationView(R.id.Main)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}