package com.example.myapplication_3.useCase.expense

import com.example.myapplication_3.entities.ExpenseItem
import com.example.myapplication_3.repository.ExpenseRepositoryImpl

class UpdateExpenseUseCase(private val expenseRepository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(position: Int, newExpense: ExpenseItem) {
        expenseRepository.updateExpense(position, newExpense)
    }
}