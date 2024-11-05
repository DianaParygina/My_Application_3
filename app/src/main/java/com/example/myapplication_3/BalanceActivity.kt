package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class BalanceActivity: AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        tvBalance = findViewById(R.id.tvBalance)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        tvBalance.text = "Ваш баланс: ${sharedFinanceViewModel.getTotalBalance()} руб"


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.listPicture -> {
//                    startActivity(Intent(this, BalanceActivity::class.java))
                    true
                }

                R.id.Main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.Income -> {
                    startActivity(Intent(this, IncomeActivity::class.java))
                    true
                }

                else -> false
            }
        }

//        updateBottomNavigationView(R.id.listPicture)
    }

//    override fun getLayoutResId(): Int {
//        return R.layout.activity_list
//    }
}


