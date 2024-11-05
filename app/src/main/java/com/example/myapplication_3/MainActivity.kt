package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider


class MainActivity : BaseMenu() {

    private lateinit var editTextAddition: EditText
    private lateinit var buttonExpence: Button
    private lateinit var SharedFinanceViewModel: SharedFinanceViewModel
//        ViewModelProvider.AndroidViewModelFactory(application)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        editTextAddition = findViewById(R.id.editTextAddition)
        buttonExpence = findViewById(R.id.buttonExpence)

        buttonExpence.setOnClickListener {
            val expenceString = editTextAddition.text.toString()
            if (expenceString.isNotEmpty()) {
                val expence = expenceString.toDoubleOrNull()
                if (expence != null) {
                    SharedFinanceViewModel.addExpense(expence)
                    showToast("Ваш баланс ${SharedFinanceViewModel.getTotalBalance()} руб")
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