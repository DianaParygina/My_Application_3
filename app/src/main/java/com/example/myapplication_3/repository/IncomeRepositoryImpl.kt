package com.example.myapplication_3.repository

import com.example.myapplication_3.frameworks.database.IncomeDatabaseHelper
import com.example.myapplication_3.entities.Income
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) {

    private val dbHelper: IncomeDatabaseHelper by lazy { IncomeDatabaseHelper(context) }

    fun getAllIncomes(): List<Income> {
        return dbHelper.getAllIncomes()
    }

    fun insertIncome(income: Income): Long {
        return dbHelper.insertIncome(income.amount, income.date, dbHelper.getIncomeTypeIdByName(dbHelper.writableDatabase, income.type) ?: 0)
    }

    fun updateIncome(income: Income, incomeId: Long) {
        dbHelper.updateIncome(income, incomeId)
    }

    fun deleteIncome(incomeId: Long) {
        dbHelper.deleteIncome(incomeId)
    }

    fun getIncomeById(incomeId: Long): Income? {
        return dbHelper.getIncomeById(incomeId)
    }
}