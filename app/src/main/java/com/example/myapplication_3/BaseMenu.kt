package com.example.myapplication_3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
abstract class BaseMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleNavigationItemClick(item.itemId)
        }
    }

    private fun handleNavigationItemClick(itemId: Int): Boolean {
        val intent: Intent? = when (itemId) {
            R.id.listPicture -> Intent(this, BalanceActivity::class.java)
            R.id.Main -> Intent(this, MainActivity::class.java)
            R.id.Income -> Intent(this, IncomeActivity::class.java)
            else -> null
        }

        intent?.let {
            // Проверяем, не пытаемся ли мы запустить текущую активность
            if (it.component?.className != this::class.java.name) {
                startActivity(it)
            }
            return true // Возвращаем true, если обработали клик
        }
        return false // Возвращаем false, если клик не обработан
    }


    abstract fun getLayoutResId(): Int

    protected fun updateBottomNavigationView(itemId: Int) { // Сделали protected
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = itemId
    }
}