package com.example.myapplication_3.UseCase.balance

import com.example.myapplication_3.repository.ExpenseRepositoryImpl
import com.example.myapplication_3.repository.IncomeRepositoryImpl

class GetTotalBalanceUseCase(
    private val incomeRepository: IncomeRepositoryImpl,
    private val expenseRepository: ExpenseRepositoryImpl
) {
    operator fun invoke(): Double {
        val totalIncome = incomeRepository.getAllIncomes().sumOf { it.amount }
        val totalExpense = expenseRepository.getAllExpenses().sumOf { it.expense }
        return totalIncome - totalExpense
    }
}