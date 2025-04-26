package com.example.myapplication_3.useCase.income

import com.example.myapplication_3.entities.Income
import com.example.myapplication_3.repository.IncomeRepositoryImpl

class GetAllIncomesUseCase(private val incomeRepository: IncomeRepositoryImpl) {
    suspend operator fun invoke(): List<Income> {
        return incomeRepository.getAllIncomes()
    }
}