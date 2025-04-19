package com.example.myapplication_3.usecase.expense

import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.repository.ExpenseRepositoryImpl

class DeleteExpenseUseCase(private val expenseRepository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(expenseItem: ExpenseItem) {
        expenseRepository.deleteExpense(expenseItem)
    }
}