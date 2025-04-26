package com.example.myapplication_3.useCase.expense

import com.example.myapplication_3.entities.ExpenseItem
import com.example.myapplication_3.repository.ExpenseRepositoryImpl

class DeleteExpenseUseCase(private val expenseRepository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(expenseItem: ExpenseItem) {
        expenseRepository.deleteExpense(expenseItem)
    }
}