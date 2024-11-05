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


class MainActivity : AppCompatActivity() {

    private lateinit var editTextAddition: EditText
    private lateinit var buttonExpence: Button
    private lateinit var SharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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

//        updateBottomNavigationView(R.id.Main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.listPicture -> {
                    startActivity(Intent(this, BalanceActivity::class.java))
                    true
                }

                R.id.Main -> {
//                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.Income -> {
                    startActivity(Intent(this, IncomeActivity::class.java))
                    true
                }

                else -> false
            }
        }

    }

//    override fun getLayoutResId(): Int {
//        return R.layout.activity_main
//    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}