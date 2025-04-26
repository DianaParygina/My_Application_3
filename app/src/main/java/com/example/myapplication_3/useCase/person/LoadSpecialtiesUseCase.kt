package com.example.myapplication_3.useCase.person

import com.example.myapplication_3.entities.Specialty
import com.example.myapplication_3.frameworks.network.ApiService
import retrofit2.awaitResponse

class LoadSpecialtiesUseCase(private val apiService: ApiService) {
    suspend operator fun invoke(page: Int, pageSize: Int): List<Specialty> {
        val response = apiService.getSpecialties(page, pageSize).awaitResponse()
        if (response.isSuccessful) {
            return response.body()?.specialties ?: emptyList()
        } else {
            throw Exception("Ошибка сервера: ${response.code()}")
        }
    }
}