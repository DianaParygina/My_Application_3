import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication_3.BD.PersonDatabaseHelper
import com.example.myapplication_3.network.Specialty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.http2.ErrorCode
import okhttp3.internal.http2.StreamResetException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import retrofit2.awaitResponse


class PersonViewModel(private val dbHelper: PersonDatabaseHelper, private val context: Context) : ViewModel() {
    val personsLiveData = MutableLiveData<List<Person>>()
    private val random = Random(System.currentTimeMillis())
    private var generationJob: Job? = null


    private val apiService = ApiUtils.getApiService(context)
    val personsLiveDataApi = MutableLiveData<List<Person>>()
    val specialtiesLiveDataApi = MutableLiveData<List<Specialty>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()
    private var currentCall: Call<*>? = null
    private var loadPersonsJob: Job? = null
    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingSpecialties = false

    init {
        loadPersons()
    }


    fun loadSpecialties() {
        isLoadingSpecialties = true

        viewModelScope.launch {
            isLoading.postValue(true)
            errorMessage.postValue(null)

            try {
                val response = apiService.getSpecialties(currentPage, 100).awaitResponse()

                if (response.isSuccessful) {
                    val specialtiesResponse = response.body()!!
                    specialtiesLiveDataApi.postValue(specialtiesResponse.specialties)

                    isLastPage = specialtiesResponse.currentPage >= specialtiesResponse.totalPages
                    currentPage = if (!isLastPage) specialtiesResponse.currentPage + 1 else specialtiesResponse.currentPage
                } else {
                    handleError(Throwable("Ошибка сервера: ${response.code()}"))
                }

            } catch (e: Exception) {
                handleFailure(e)
            } finally {
                isLoading.postValue(false)
                isLoadingSpecialties = false
            }
        }
    }


    fun loadPersonsFromNetwork(specialtyId: Int? = null) {
        loadPersonsJob?.cancel()

        loadPersonsJob = viewModelScope.launch {
            isLoading.postValue(true)
            errorMessage.postValue(null)

            try {
                if (specialtyId != null) {
                    delay(3000L)
                    withContext(Dispatchers.IO) {
                        val call = apiService.getPersonsBySpecialty(specialtyId)
                        currentCall = call

                        call.enqueue(object : Callback<List<Person>> {
                            override fun onResponse(call: Call<List<Person>>, response: Response<List<Person>>)
                            {
                                isLoading.postValue(false)
                                currentCall = null


                                if (response.isSuccessful) {
                                    val persons = response.body() ?: emptyList()
                                    Log.d("Network", "Persons loaded: $persons")
                                    personsLiveDataApi.postValue(persons)
                                } else {
                                    handleError(Throwable("Ошибка сервера при загрузке персон: ${response.code()}"))
                                }
                            }

                            override fun onFailure(call: Call<List<Person>>, t: Throwable) {
                                isLoading.postValue(false)
                                currentCall = null
                                handleFailure(t)
                            }
                        })
                    }
                }
            } catch (e: Exception) {
                handleFailure(e)
            } finally {
                loadPersonsJob = null
            }
        }
    }


    private fun handleFailure(t: Throwable) {
        if (t is IOException) {
            handleNetworkError(t)
        } else {
            handleError(t)
        }
    }

    private fun handleNetworkError(e: IOException) {
        if (e is SocketTimeoutException) {
            errorMessage.postValue("Timeout error")
        } else if (e.message == "Canceled") {
            Log.d("Network", "Request was cancelled")
        } else {
            errorMessage.postValue("Network error: ${e.message}")
            Log.e("Network", "Network Error", e)
        }
        isLoading.postValue(false)
    }


    private fun handleError(error: Throwable) {
        val errorMessageText = when (error) {
            is SocketTimeoutException -> "Timeout error"
            is IOException -> "Network error: ${error.message}"
            else -> "Error: ${error.message}"
        }
        errorMessage.postValue(errorMessageText)
        Log.e("Network", errorMessageText, error)
    }






    fun loadPersons() {
        viewModelScope.launch {
            val persons = withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor: Cursor = db.rawQuery("""
                SELECT ${PersonDatabaseHelper.TABLE_PERSONS}.*, ${PersonDatabaseHelper.TABLE_SPECIALTY}.${PersonDatabaseHelper.COL_SPECIALTY_TITLE}
                FROM ${PersonDatabaseHelper.TABLE_PERSONS}
                JOIN ${PersonDatabaseHelper.TABLE_SPECIALTY} ON ${PersonDatabaseHelper.TABLE_PERSONS}.${PersonDatabaseHelper.COL_PERSON_TYPE_SPECIALTY_ID} = ${PersonDatabaseHelper.TABLE_SPECIALTY}.${PersonDatabaseHelper.COL_SPECIALTY_ID}
            """, null)

                val personsList = mutableListOf<Person>()

                while (cursor.moveToNext()) {
                    val person = Person(
                        cursor.getLong(cursor.getColumnIndexOrThrow(PersonDatabaseHelper.COL_PERSON_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PersonDatabaseHelper.COL_PERSON_FIRST_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PersonDatabaseHelper.COL_PERSON_LAST_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PersonDatabaseHelper.COL_PERSON_YEAR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PersonDatabaseHelper.COL_PERSON_YEAR_OF_ADMISSION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PersonDatabaseHelper.COL_SPECIALTY_TITLE))
                    )
                    personsList.add(person)
                }
                cursor.close()
                personsList
            }
            personsLiveData.postValue(persons)
        }
    }


    fun generateAndInsertData(count: Int) {
        generationJob?.cancel()
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            try {
                db.beginTransaction()
                for (i in 1..count) {
                    if (!isActive) break

                    val specialtyTitle = "Специальность-${UUID.randomUUID()}"

                    val specialtyValues = ContentValues().apply {
                        put(PersonDatabaseHelper.COL_SPECIALTY_TITLE, specialtyTitle)
                    }

                    val newSpecialtyId = db.insert(PersonDatabaseHelper.TABLE_SPECIALTY, null, specialtyValues)

                    if (newSpecialtyId != -1L) {
                        val firstName = "Имя $i"
                        val lastName = "Фамилия $i"
                        val year = random.nextInt(1970, 2024)
                        val yearOfAdmission = random.nextInt(2010, 2024)

                        val personValues = ContentValues().apply {
                            put(PersonDatabaseHelper.COL_PERSON_FIRST_NAME, firstName)
                            put(PersonDatabaseHelper.COL_PERSON_LAST_NAME, lastName)
                            put(PersonDatabaseHelper.COL_PERSON_YEAR, year)
                            put(PersonDatabaseHelper.COL_PERSON_YEAR_OF_ADMISSION, yearOfAdmission)
                            put(PersonDatabaseHelper.COL_PERSON_TYPE_SPECIALTY_ID, newSpecialtyId.toInt())
                        }
                        db.insert(PersonDatabaseHelper.TABLE_PERSONS, null, personValues)
                    }
                }
                if (isActive) {
                    db.setTransactionSuccessful()
                }
            } finally {
                if (db.inTransaction()) {
                    db.endTransaction()
                }
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        loadPersons()
                    }
                }
            }
        }
    }

    fun clearData() = viewModelScope.launch(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        dbHelper.clearData(db)
        db.close()
        loadPersons()
    }

    fun stopDataGeneration() {
        generationJob?.cancel()
    }



    fun clearErrorMessage() {
        errorMessage.value = null
    }






    fun cancelRequests() {
        loadPersonsJob?.cancel() // Отмена корутины
        loadPersonsJob = null
        currentCall?.cancel() // Отмена вызова Retrofit
        currentCall = null
        isLoading.postValue(false)
    }
}