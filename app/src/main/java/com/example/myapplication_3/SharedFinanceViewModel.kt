package com.example.myapplication_3

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SharedFinanceViewModel(application: Application) : AndroidViewModel(application) {
    private var totalIncome = 0.0
    private var totalExpense = 0.0

    private val _totalBalance = MutableLiveData<Double>(0.0)

    fun addIncome(amount: Double) {
        totalIncome += amount
        updateTotalBalance()
    }

    fun addExpense(amount: Double) {
        totalExpense += amount
        updateTotalBalance()
    }

    private fun updateTotalBalance() {
        _totalBalance.value = totalIncome - totalExpense
    }

    fun getTotalBalance(): Double {
        return _totalBalance.value ?: 0.0
    }

    fun getTotalIncome(): Double {
        return totalIncome
    }

    fun getTotalExpense(): Double {
        return totalExpense
    }
}