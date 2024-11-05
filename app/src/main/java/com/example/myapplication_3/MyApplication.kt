package com.example.myapplication_3

import android.app.Application
import androidx.lifecycle.ViewModelProvider

class MyApplication : Application() {

    val sharedFinanceViewModel: SharedFinanceViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory(this).create(SharedFinanceViewModel::class.java)
    }
}