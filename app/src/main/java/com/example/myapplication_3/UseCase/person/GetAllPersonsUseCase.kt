package com.example.myapplication_3.UseCase.person

import com.example.myapplication_3.Entities.Person
import com.example.myapplication_3.Frameworks.database.PersonDatabaseHelper

class GetAllPersonsUseCase(private val dbHelper: PersonDatabaseHelper) {
    operator fun invoke(): List<Person> {
        return dbHelper.getAllPersons()
    }
}
