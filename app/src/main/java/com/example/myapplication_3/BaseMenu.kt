//package com.example.myapplication_3
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//abstract class BaseMenu : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(getLayoutResId()) // Устанавливаем макет для каждой активности
//
//
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.listPicture -> {
//                        startActivity(Intent(this, BalanceActivity::class.java))
//                    true
//                }
//
//                R.id.Main -> {
//                        startActivity(Intent(this, MainActivity::class.java))
//                    true
//                }
//
//                R.id.Income -> {
//                        startActivity(Intent(this, IncomeActivity::class.java))
//                    true
//                }
//
//                else -> false
//            }
//        }
//    }
//
//    abstract fun getLayoutResId(): Int
//
//    fun updateBottomNavigationView(itemId: Int) {
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//        bottomNavigationView.selectedItemId = itemId
//    }
//}