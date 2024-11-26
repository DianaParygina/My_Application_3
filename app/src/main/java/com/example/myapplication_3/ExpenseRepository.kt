package com.example.myapplication_3

import android.content.Context
import com.example.myapplication_3.fileTools.XLSFileHandler

object ExpenseRepository {

    private const val XLS_FILE_NAME = "expenses.xls"

    fun initialize(context: Context) {
        XLSFileHandler.initialize(context, XLS_FILE_NAME)
    }

    fun getAllExpenses(): List<ExpenseItem> {
        return XLSFileHandler.loadDataFromXLS()
    }

    fun addExpense(expenseItem: ExpenseItem) {
        XLSFileHandler.addLineToXLS(expenseItem)
    }

    fun updateExpense(oldExpense: ExpenseItem, newExpense: ExpenseItem) {
        XLSFileHandler.updateLineInXLS(oldExpense, newExpense)
    }

    fun deleteExpense(expenseItem: ExpenseItem) {
        XLSFileHandler.deleteLineFromXLS(expenseItem)
    }
}