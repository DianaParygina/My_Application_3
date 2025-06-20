package com.example.myapplication_3.useCase.income

import com.example.myapplication_3.repository.IncomeRepositoryImpl

class DeleteIncomeUseCase(private val incomeRepository: IncomeRepositoryImpl) {
    suspend operator fun invoke(incomeId: Long) {
        incomeRepository.deleteIncome(incomeId)
    }
}