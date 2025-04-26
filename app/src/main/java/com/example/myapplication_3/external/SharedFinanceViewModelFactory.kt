//package com.example.myapplication_3.External
//
//import android.app.Application
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.myapplication_3.SharedFinanceViewModel
//import com.example.myapplication_3.repository.ExpenseRepositoryImpl
//import com.example.myapplication_3.repository.IncomeRepositoryImpl
//
//class SharedFinanceViewModelFactory(
//    private val application: Application,
//    private val incomeRepository: IncomeRepositoryImpl,
//    private val expenseRepository: ExpenseRepositoryImpl
//) : ViewModelProvider.Factory {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(SharedFinanceViewModel::class.java)) {
//            return SharedFinanceViewModel(application, incomeRepository, expenseRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}