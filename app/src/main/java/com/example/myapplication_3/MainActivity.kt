package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : BaseMenu() {

    private lateinit var editTextAddition: EditText
    private lateinit var buttonExpence: Button
    private lateinit var SharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        editTextAddition = findViewById(R.id.editTextAddition)
        buttonExpence = findViewById(R.id.buttonExpence)

        SharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel


        buttonExpence.setOnClickListener {
            val expenceString = editTextAddition.text.toString()
            if (expenceString.isNotEmpty()) {
                val expence = expenceString.toDoubleOrNull()
                if (expence != null) {
                    SharedFinanceViewModel.addExpense(expence)
                    showToast("Ваш расход ${SharedFinanceViewModel.getTotalExpense()} руб")
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