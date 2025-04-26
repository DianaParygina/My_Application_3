package com.example.myapplication_3.useCase.person

import com.example.myapplication_3.frameworks.database.PersonDatabaseHelper

class ClearPersonsUseCase(private val dbHelper: PersonDatabaseHelper) {
    operator fun invoke() {
        dbHelper.clearAllData()
    }
}