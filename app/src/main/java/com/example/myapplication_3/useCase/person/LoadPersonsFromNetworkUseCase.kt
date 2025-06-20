package com.example.myapplication_3.useCase.person

import com.example.myapplication_3.entities.Person
import com.example.myapplication_3.frameworks.network.ApiService
import retrofit2.awaitResponse

class LoadPersonsFromNetworkUseCase(private val apiService: ApiService) {
    suspend operator fun invoke(specialtyId: Int): List<Person> {
        val response = apiService.getPersonsBySpecialty(specialtyId).awaitResponse()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Ошибка сервера: ${response.code()}")
        }
    }
}