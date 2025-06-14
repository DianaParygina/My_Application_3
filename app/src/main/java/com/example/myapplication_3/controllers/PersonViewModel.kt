package com.example.myapplication_3.controllers

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication_3.entities.Person
import com.example.myapplication_3.entities.Specialty
import com.example.myapplication_3.frameworks.database.PersonDatabaseHelper
import com.example.myapplication_3.frameworks.network.ApiUtils
import com.example.myapplication_3.useCase.person.GetAllPersonsUseCase
import com.example.myapplication_3.useCase.person.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val dbHelper: PersonDatabaseHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val getAllPersonsUseCase = GetAllPersonsUseCase(dbHelper)
    private val generatePersonsUseCase = GeneratePersonsUseCase(dbHelper)
    private val clearPersonsUseCase = ClearPersonsUseCase(dbHelper)
    private val loadPersonsFromNetworkUseCase = LoadPersonsFromNetworkUseCase(ApiUtils.getApiService(context))
    private val loadSpecialtiesUseCase = LoadSpecialtiesUseCase(ApiUtils.getApiService(context))

    val personsLiveData = MutableLiveData<List<Person>>()
    val personsLiveDataApi = MutableLiveData<List<Person>>()
    val specialtiesLiveDataApi = MutableLiveData<List<Specialty>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    private var generationJob: Job? = null
    private var loadPersonsJob: Job? = null
    private var currentPage = 1

    init {
        loadPersons()
    }

    fun loadPersons() {
        viewModelScope.launch {
            personsLiveData.value = withContext(Dispatchers.IO) {
                getAllPersonsUseCase()
            }
        }
    }

    fun generateAndInsertData(count: Int) {
        generationJob?.cancel()
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            generatePersonsUseCase(count)
            loadPersons()
        }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO) {
            clearPersonsUseCase()
            loadPersons()
        }
    }

    fun loadSpecialties() {
        isLoading.value = true
        errorMessage.value = null

        loadPersonsJob = viewModelScope.launch {
            try {
                val specialties = loadSpecialtiesUseCase(currentPage, 100)
                specialtiesLiveDataApi.value = specialties
            } catch (e: Exception) {
                errorMessage.value = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadPersonsFromNetwork(specialtyId: Int) {
        isLoading.value = true
        errorMessage.value = null

        loadPersonsJob = viewModelScope.launch {
            try {
                val persons = loadPersonsFromNetworkUseCase(specialtyId)
                personsLiveDataApi.value = persons
            } catch (e: Exception) {
                errorMessage.value = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun stopDataGeneration() {
        generationJob?.cancel()
    }

    fun cancelRequests() {
        loadPersonsJob?.cancel()
        isLoading.value = false
    }

    fun clearErrorMessage() {
        errorMessage.value = null
    }
}