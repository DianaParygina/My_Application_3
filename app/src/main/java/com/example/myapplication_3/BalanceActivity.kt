package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class BalanceActivity: BaseMenu() {

    private lateinit var tvBalance: TextView
    private lateinit var SharedFinanceViewModel: SharedFinanceViewModel
//        ViewModelProvider.AndroidViewModelFactory(application)
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        tvBalance = findViewById(R.id.tvBalance)

        SharedFinanceViewModel.getTotalBalance()

        tvBalance.text = "Ваш баланс: ${SharedFinanceViewModel.getTotalBalance()} руб"


        updateBottomNavigationView(R.id.listPicture)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_list
    }
}


