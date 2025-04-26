package com.example.myapplication_3.useCase.person

import com.example.myapplication_3.frameworks.database.PersonDatabaseHelper
import kotlin.math.min

class GeneratePersonsUseCase(private val dbHelper: PersonDatabaseHelper) {
    operator fun invoke(count: Int) {
        val limitedCount = min(count, 5)
        if (limitedCount <= 0) {
            return
        }
        dbHelper.generateData(limitedCount)
    }
}