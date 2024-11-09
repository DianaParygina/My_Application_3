package com.example.myapplication_3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IncomeActivity : BaseMenu() {

    private lateinit var editTextIncome: EditText
    private lateinit var buttonIncome: Button
    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editTextIncome = findViewById(R.id.editTextIncome)
        buttonIncome = findViewById(R.id.buttonIncome)
        incomeAdapter = IncomeAdapter(mutableListOf())

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        val incomeItem = IncomeAdapter(mutableListOf())

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = incomeItem

        buttonIncome.setOnClickListener {
            val incomeText = editTextIncome.text.toString()
            if (incomeText.isNotEmpty()) {
                val income = incomeText.toDoubleOrNull()
                if (income != null) {
                    sharedFinanceViewModel.addIncome(income)
                    incomeAdapter.addIncome(incomeItem.toString() + "руб")
                    showToast("Ваш доход ${sharedFinanceViewModel.getTotalIncome()} руб")
                    editTextIncome.text.clear()
                } else {
                    showToast("Введите корректное число")
                }
            } else {
                showToast("Введите сумму дохода")
            }
        }
        updateBottomNavigationView(R.id.Income)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_income
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}