package com.example.myapplication_3.UseCase.income

import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.repository.IncomeRepositoryImpl

class GetAllIncomesUseCase(private val incomeRepository: IncomeRepositoryImpl) {
    suspend operator fun invoke(): List<Income> {
        return incomeRepository.getAllIncomes()
    }
}