package com.example.myapplication_3.Controllers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.repository.ExpenseRepositoryImpl
import com.example.myapplication_3.repository.IncomeRepositoryImpl
import com.example.myapplication_3.usecase.balance.GetTotalBalanceUseCase
import com.example.myapplication_3.usecase.expense.*
import com.example.myapplication_3.usecase.income.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedFinanceViewModel(
    application: Application,
    private val incomeRepository: IncomeRepositoryImpl,
    private val expenseRepository: ExpenseRepositoryImpl
) : AndroidViewModel(application) {

    private val getTotalBalanceUseCase = GetTotalBalanceUseCase(incomeRepository, expenseRepository)

    private val getAllIncomesUseCase = GetAllIncomesUseCase(incomeRepository)
    private val addIncomeUseCase = AddIncomeUseCase(incomeRepository)
    private val deleteIncomeUseCase = DeleteIncomeUseCase(incomeRepository)
    private val updateIncomeUseCase = UpdateIncomeUseCase(incomeRepository)

    private val getAllExpensesUseCase = GetAllExpensesUseCase(expenseRepository)
    private val addExpenseUseCase = AddExpenseUseCase(expenseRepository)
    private val deleteExpenseUseCase = DeleteExpenseUseCase(expenseRepository)
    private val updateExpenseUseCase = UpdateExpenseUseCase(expenseRepository)

    private var totalIncome = 0.0
    private var totalExpense = 0.0

    val totalBalance: LiveData<Double>
        get() = _totalBalance
    private val _totalBalance = MutableLiveData<Double>(0.0)

    init {
        viewModelScope.launch {
            _totalBalance.value = withContext(Dispatchers.IO) { getTotalBalanceUseCase() }
        }

        // Загрузка данных из репозиториев
        viewModelScope.launch {
            totalIncome = getAllIncomesUseCase().sumOf { it.amount }
            totalExpense = getAllExpensesUseCase().sumOf { it.expense }
            updateTotalBalance()
        }
    }

    fun addIncome(amount: Double, date: String, type: String) {
        viewModelScope.launch {
            val income = Income(0, amount, date, type)
            addIncomeUseCase(income)
            totalIncome += amount
            updateTotalBalance()
        }
    }

    fun addExpense(amount: Double, date: String, type: String) {
        viewModelScope.launch {
            val expenseItem = ExpenseItem(amount, date, type)
            addExpenseUseCase(expenseItem)
            totalExpense += amount
            updateTotalBalance()
        }
    }

    fun deleteExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
            totalExpense -= expense.expense
            updateTotalBalance()
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            income.id?.let { deleteIncomeUseCase(it) }
            totalIncome -= income.amount
            updateTotalBalance()
        }
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