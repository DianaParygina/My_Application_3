package com.example.myapplication_3.repository

import com.example.myapplication_3.Entities.ExpenseItem
import com.example.myapplication_3.Frameworks.files.XLSFileHandler
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) {

    init {
        XLSFileHandler.initialize(context, "expenses.xls")
    }

    fun getAllExpenses(): List<ExpenseItem> {
        return XLSFileHandler.loadDataFromXLS()
    }

    fun addExpense(expenseItem: ExpenseItem) {
        XLSFileHandler.addLineToXLS(expenseItem)
    }

    fun updateExpense(position: Int, newLine: ExpenseItem) {
        XLSFileHandler.updateLineInXLS(position, newLine)
    }

    fun deleteExpense(expenseItem: ExpenseItem) {
        XLSFileHandler.deleteLineFromXLS(expenseItem)
    }
}