package com.example.myapplication_3.UseCase.expense

import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.repository.ExpenseRepositoryImpl
import java.lang.IllegalArgumentException

class AddExpenseUseCase(private val expenseRepository: ExpenseRepositoryImpl) {
    suspend operator fun invoke(expenseItem: ExpenseItem){
        if (expenseItem.expense <= 0) {
            throw IllegalArgumentException("Сумма дохода должна быть положительным числом")
        }
        expenseRepository.addExpense(expenseItem)
    }
}