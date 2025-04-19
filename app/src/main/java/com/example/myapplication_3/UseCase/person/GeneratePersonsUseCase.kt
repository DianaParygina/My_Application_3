package com.example.myapplication_3.UseCase.person

import com.example.myapplication_3.Frameworks.database.PersonDatabaseHelper
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