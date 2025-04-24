package com.example.myapplication_3.Controllers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.repository.ExpenseRepositoryImpl
import com.example.myapplication_3.repository.IncomeRepositoryImpl
import com.example.myapplication_3.UseCase.balance.*
import com.example.myapplication_3.UseCase.expense.*
import com.example.myapplication_3.UseCase.income.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharedFinanceViewModel @Inject constructor(
    private val incomeRepository: IncomeRepositoryImpl,
    private val expenseRepository: ExpenseRepositoryImpl
) : ViewModel() {

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

    private val _totalBalance = MutableLiveData<Double>(0.0)
    val totalBalance: LiveData<Double> = _totalBalance

    init {
        viewModelScope.launch {
            _totalBalance.value = withContext(Dispatchers.IO) { getTotalBalanceUseCase() }

            val incomeItems = withContext(Dispatchers.IO) { getAllIncomesUseCase() }
            totalIncome = incomeItems.sumOf { it.amount }

            val expenseItems = withContext(Dispatchers.IO) { getAllExpensesUseCase() }
            totalExpense = expenseItems.sumOf { it.expense }
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
            income.id?.let {
                withContext(Dispatchers.IO) { deleteIncomeUseCase(it) }
                totalIncome -= income.amount
                updateTotalBalance()
            }
        }
    }


    private fun updateTotalBalance() {
        viewModelScope.launch(Dispatchers.Main) {
            _totalBalance.value = totalIncome - totalExpense
        }
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