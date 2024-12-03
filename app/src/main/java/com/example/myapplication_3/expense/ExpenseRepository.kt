//package com.example.myapplication_3.expense
//
//import android.content.Context
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//
//
//object ExpenseRepository {
//
//    private const val XLS_FINANCE_FILE = "finance.xls"
//
//    private var xlsFileHandler: XLSFileHandler? = null
//
//    private val _expenses = MutableLiveData<List<ExpenseItem>>(emptyList())
//    val expenses: LiveData<List<ExpenseItem>> = _expenses
//
//
//    fun initialize(context: Context) {
//        xlsFileHandler = XLSFileHandler
//        xlsFileHandler?.initialize(context, XLS_FINANCE_FILE)
//        loadExpensesFromXLS()
//    }
//
//
//    private fun loadExpensesFromXLS() {
//        _expenses.value = xlsFileHandler?.loadDataFromXLS() ?: emptyList()
//    }
//
//
//    fun getAllExpense(): List<ExpenseItem> {
//        return xlsFileHandler?.loadDataFromXLS() ?: emptyList()
//    }
//
//
//    fun addExpense(expense: ExpenseItem) {
//        xlsFileHandler?.addLineToXLS(expense)
//        loadExpensesFromXLS()
//    }
//
//
//    fun deleteExpense(expense: ExpenseItem){
//        val expenses = getAllExpense().toMutableList()
//        val indexToRemove = expenses.indexOfFirst {
//            it.expense == expense.expense && it.date == expense.date && it.type == expense.type
//        }
//
//        if (indexToRemove != -1) {
//            val expenseToRemove = expenses[indexToRemove]
//            val stringToRemove = "${expenseToRemove.expense},${expenseToRemove.date},${expenseToRemove.type}"
//
//            xlsFileHandler?.deleteLineFromXLS(stringToRemove)
//        }
//        loadExpensesFromXLS()
//    }
//
//
//    fun editExpense(oldExpense: ExpenseItem, newExpense: ExpenseItem){
//        val expenses = getAllExpense().toMutableList()
//        val index = expenses.indexOf(oldExpense)
//
//        if (index != -1) {
//            expenses[index] = newExpense
//            val oldString = "${oldExpense.expense},${oldExpense.date},${oldExpense.type}"
//            xlsFileHandler?.deleteLineFromXLS(oldString)
//            xlsFileHandler?.addLineToXLS(newExpense)
//        }
//        loadExpensesFromXLS()
//    }
//}