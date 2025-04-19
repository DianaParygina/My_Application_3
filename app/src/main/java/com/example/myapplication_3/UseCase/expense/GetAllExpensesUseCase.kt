package com.example.myapplication_3.UseCase.expense

import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.repository.ExpenseRepositoryImpl

class GetAllExpensesUseCase(private val expenseRepository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(): List<ExpenseItem> {
        return expenseRepository.getAllExpenses()
    }
}