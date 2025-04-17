//package com.example.myapplication_3
//
//import android.os.Bundle
//import android.widget.TextView
//import androidx.lifecycle.Observer
//
//class BalanceActivity: BaseMenu() {
//
//    private lateinit var tvBalance: TextView
//    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        tvBalance = findViewById(R.id.tvBalance)
//
//        sharedFinanceViewModel = (application as MyApplication).sharedFinanceViewModel
//
////        tvBalance.text = "Ваш баланс: ${sharedFinanceViewModel.getTotalBalance()} руб"
//
//        // Подписываемся на изменения баланса
//        sharedFinanceViewModel.totalBalance.observe(this, Observer { newBalance ->
//            tvBalance.text = "Ваш баланс: ${newBalance} руб"
//        })
//
//        updateBottomNavigationView(R.id.listPicture)
//    }
//
//    override fun getLayoutResId(): Int {
//        return R.layout.activity_list
//    }
//}