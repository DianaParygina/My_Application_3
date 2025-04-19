package com.example.myapplication_3.usecase.income

import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.repository.IncomeRepositoryImpl
import java.lang.IllegalArgumentException

class AddIncomeUseCase(private val incomeRepository: IncomeRepositoryImpl) {
    suspend operator fun invoke(income: Income): Long {
        if (income.amount <= 0) {
            throw IllegalArgumentException("Сумма дохода должна быть положительным числом")
        }
        return incomeRepository.insertIncome(income)
    }
}