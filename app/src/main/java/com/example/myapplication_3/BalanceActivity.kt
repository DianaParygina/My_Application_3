package com.example.myapplication_3

import android.os.Bundle
import android.widget.TextView

class BalanceActivity: BaseMenu() {

    private lateinit var tvBalance: TextView
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvBalance = findViewById(R.id.tvBalance)

        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel

        tvBalance.text = "Ваш баланс: ${sharedFinanceViewModel.getTotalBalance()} руб"

        updateBottomNavigationView(R.id.listPicture)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_list
    }
}