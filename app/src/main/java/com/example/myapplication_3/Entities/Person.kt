package com.example.myapplication_3.Entities
data class Person(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val year: Int,
    val yearOfAdmission: Int,
    val specialtyTitle: String? = null
)