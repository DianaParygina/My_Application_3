package com.example.myapplication_3.frameworks.network

import com.example.myapplication_3.entities.Person
import com.example.myapplication_3.entities.Specialty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/specialties")
    fun getSpecialties(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<SpecialtiesResponse>

    @GET("/persons/specialty/{specialtyId}")
    fun getPersonsBySpecialty(@Path("specialtyId") specialtyId: Int): Call<List<Person>>
}

data class SpecialtiesResponse(
    val specialties: List<Specialty>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
)