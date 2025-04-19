package com.example.myapplication_3.External

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication_3.Controllers.PersonViewModel
import com.example.myapplication_3.Frameworks.database.PersonDatabaseHelper

class PersonViewModelFactory(
    private val dbHelper: PersonDatabaseHelper,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonViewModel(dbHelper, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}