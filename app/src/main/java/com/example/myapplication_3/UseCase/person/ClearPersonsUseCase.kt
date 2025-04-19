package com.example.myapplication_3.UseCase.person

import com.example.myapplication_3.Frameworks.database.PersonDatabaseHelper

class ClearPersonsUseCase(private val dbHelper: PersonDatabaseHelper) {
    operator fun invoke() {
        dbHelper.clearAllData()
    }
}