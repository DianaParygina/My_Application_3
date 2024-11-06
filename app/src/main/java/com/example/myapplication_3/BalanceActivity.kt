package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

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


