package com.example.myapplication_3.usecase.income

import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.repository.IncomeRepositoryImpl

class UpdateIncomeUseCase(private val incomeRepository: IncomeRepositoryImpl) {
    suspend operator fun invoke(income: Income, incomeId: Long) {
        incomeRepository.updateIncome(income, incomeId)
    }
}