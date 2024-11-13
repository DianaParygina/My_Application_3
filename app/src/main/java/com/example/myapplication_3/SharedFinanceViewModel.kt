package com.example.myapplication_3

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle

class SharedFinanceViewModel(application: Application) : AndroidViewModel(application) {
    private var totalIncome = 0.0
    private var totalExpense = 0.0

    private val _totalBalance = MutableLiveData<Double>(0.0)

    // Добавлен блок init
    init {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Загрузка доходов
        val incomeString = sharedPrefs.getString("incomeList", "")
        val incomeItems = incomeString?.split(",")?.mapNotNull { it.replace("руб", "").trim().toDoubleOrNull() } ?: emptyList()
        totalIncome = incomeItems.sum()

        // Загрузка расходов
        val expensesString = sharedPrefs.getString("expenseList", "")
        val expenseItems = expensesString?.split(",")?.mapNotNull { it.replace("руб", "").trim().toDoubleOrNull() } ?: emptyList()
        totalExpense = expenseItems.sum()

        updateTotalBalance() // Обновляем баланс после загрузки данных
    }

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

    fun deleteExpense(amount: Double) {
        totalExpense -= amount
        updateTotalBalance()
    }

    fun deleteIncome(amount: Double) {
        totalIncome -= amount
        updateTotalBalance()
    }
}