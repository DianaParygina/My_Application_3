package com.example.myapplication_3.useCase.income

import com.example.myapplication_3.entities.Income
import com.example.myapplication_3.repository.IncomeRepositoryImpl

class UpdateIncomeUseCase(private val incomeRepository: IncomeRepositoryImpl) {
    suspend operator fun invoke(income: Income, incomeId: Long) {
        incomeRepository.updateIncome(income, incomeId)
    }
}