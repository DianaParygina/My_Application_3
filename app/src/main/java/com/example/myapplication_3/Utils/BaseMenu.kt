package com.example.myapplication_3.Utils

import com.example.myapplication_3.Frameworks.ui.BalanceFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication_3.Frameworks.ui.ExpenseFragment
//import com.example.myapplication_3.Frameworks.ui.MainActivity
//import com.example.myapplication_3.finance.ExpenseFragment
import com.example.myapplication_3.Frameworks.ui.IncomeFragment
//import com.example.myapplication_3.income.IncomeActivity
//import com.example.myapplication_3.person.PersonActivity
import com.example.myapplication_3.Frameworks.ui.PersonFragment
import com.example.myapplication_3.R
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleNavigationItemClick(item.itemId)
        }

        if (savedInstanceState == null) {
            handleNavigationItemClick(R.id.listPicture)
        }
    }

    private fun handleNavigationItemClick(itemId: Int): Boolean {
        return when (itemId) {
            R.id.listPicture -> { replaceFragment(BalanceFragment()); true }
            R.id.Main -> { replaceFragment(ExpenseFragment()); true }
            R.id.Income -> { replaceFragment(IncomeFragment()); true }
            R.id.Generate -> { replaceFragment(PersonFragment()); true }
            else -> false
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    abstract fun getLayoutResId(): Int

    protected fun updateBottomNavigationView(itemId: Int) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = itemId
    }
}