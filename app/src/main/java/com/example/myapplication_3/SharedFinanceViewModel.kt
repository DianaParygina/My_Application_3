package com.example.myapplication_3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedFinanceViewModel : ViewModel() {
    private var totalIncome = 0.0
    private var totalExpense = 0.0

    private val _totalBalance = MutableLiveData<Double>(0.0)
    val totalBalance: LiveData<Double> = _totalBalance

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