package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class IncomeActivity : AppCompatActivity() {

    private lateinit var editTextIncome: EditText
    private lateinit var buttonIncome: Button
    private val SharedFinanceViewModel: SharedFinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        editTextIncome = findViewById(R.id.editTextIncome)
        buttonIncome = findViewById(R.id.buttonIncome)

        buttonIncome.setOnClickListener{
            val incomeText = editTextIncome.text.toString()
            if(incomeText.isNotEmpty()){
                val income = incomeText.toDoubleOrNull()
                if(income != null){
                    SharedFinanceViewModel.addIncome(income)
                    showToast("Ваш доход ${SharedFinanceViewModel.getTotalIncome()} руб")
                    editTextIncome.text.clear()

                }else{
                    showToast("Введитк корректное число")
                }
            }else {
                showToast("ведите сумму дохода")
            }
        }
//        updateBottomNavigationView(R.id.Income)


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
//                    startActivity(Intent(this, IncomeActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

//    override fun getLayoutResId(): Int {
//        return R.layout.activity_income
//    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}