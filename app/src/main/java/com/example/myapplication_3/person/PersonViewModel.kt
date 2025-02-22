import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication_3.BD.PersonDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.database.Cursor
import com.example.myapplication_3.person.Person
import java.util.UUID
import kotlin.random.Random

class PersonViewModel(private val dbHelper: PersonDatabaseHelper) : ViewModel() {
    val personsLiveData = MutableLiveData<List<Person>>()
    private val random = Random(System.currentTimeMillis())

    init {
        loadPersons() // Загружаем данные при создании ViewModel
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
//                db.close()
                personsList
            }
            personsLiveData.postValue(persons)

        }
    }


    fun generateAndInsertData(count: Int) {
        val db = dbHelper.writableDatabase

        db.beginTransaction()
        try {
            for (i in 1..count) {
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
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            loadPersons()

        }
//        db.close()
    }

    fun clearData() = viewModelScope.launch(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        dbHelper.clearData(db)
        db.close()
        loadPersons() // Перезагружаем данные после очистки (чтобы обновить UI)
    }
}