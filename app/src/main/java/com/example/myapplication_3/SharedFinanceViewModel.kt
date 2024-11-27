package com.example.myapplication_3

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.myapplication_3.expense.XLSFileHandler
import com.example.myapplication_3.income.BinFileHandler

class SharedFinanceViewModel(application: Application) : AndroidViewModel(application) {
    private var totalIncome = 0.0
    private var totalExpense = 0.0

    val totalBalance: LiveData<Double>
        get() = _totalBalance
    private val _totalBalance = MutableLiveData<Double>(0.0)

    init {
        BinFileHandler.initialize(application.applicationContext, "incomes.bin")
        XLSFileHandler.initialize(application.applicationContext, "expenses.xls")

        // Загрузка доходов из бинарного файла
        val incomeItems = BinFileHandler.loadDataFromBin()
        totalIncome = incomeItems.sumOf { it.amount }

        // Загрузка расходов из XLS файла
        val expenseItems = XLSFileHandler.loadDataFromXLS()
        totalExpense = expenseItems.sumOf { it.expense }


        updateTotalBalance()
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