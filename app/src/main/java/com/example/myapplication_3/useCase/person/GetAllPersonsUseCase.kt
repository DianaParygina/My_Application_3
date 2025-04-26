package com.example.myapplication_3.useCase.person

import com.example.myapplication_3.entities.Person
import com.example.myapplication_3.frameworks.database.PersonDatabaseHelper

class GetAllPersonsUseCase(private val dbHelper: PersonDatabaseHelper) {
    operator fun invoke(): List<Person> {
        return dbHelper.getAllPersons()
    }
}
