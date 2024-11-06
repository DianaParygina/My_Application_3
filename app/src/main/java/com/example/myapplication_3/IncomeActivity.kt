package com.example.myapplication_3

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class IncomeActivity : BaseMenu() {

    private lateinit var editTextIncome: EditText
    private lateinit var buttonIncome: Button
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editTextIncome = findViewById(R.id.editTextIncome)
        buttonIncome = findViewById(R.id.buttonIncome)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        buttonIncome.setOnClickListener {
            val incomeText = editTextIncome.text.toString()
            if (incomeText.isNotEmpty()) {
                val income = incomeText.toDoubleOrNull()
                if (income != null) {
                    sharedFinanceViewModel.addIncome(income)
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