package com.example.myapplication_3.usecase.expense

import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.repository.ExpenseRepositoryImpl

class UpdateExpenseUseCase(private val expenseRepository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(position: Int, newExpense: ExpenseItem) {
        expenseRepository.updateExpense(position, newExpense)
    }
}