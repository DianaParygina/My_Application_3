package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class IncomeActivity : AppCompatActivity() {

    private lateinit var editTextIncome: EditText
    private lateinit var buttonIncome: Button
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        editTextIncome = findViewById(R.id.editTextIncome)
        buttonIncome = findViewById(R.id.buttonIncome)

        // Получение ViewModel
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

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.listPicture -> {
                    startActivity(Intent(this, BalanceActivity::class.java))
                    true
                }
                R.id.Main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.Income -> {
                    // Уже находимся в IncomeActivity, ничего не делаем
                    true
                }
                else -> false
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}