package com.example.myapplication_3.Frameworks.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import com.example.myapplication_3.Entities.Person
import com.example.myapplication_3.Entities.Specialty
import java.util.UUID
import kotlin.random.Random

class PersonDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "persons.db"
        const val DATABASE_VERSION = 1

        // Таблица персон
        const val TABLE_PERSONS = "persons"
        const val COL_PERSON_ID = "id"
        const val COL_PERSON_FIRST_NAME = "first_name"
        const val COL_PERSON_LAST_NAME = "last_name"
        const val COL_PERSON_YEAR = "year"
        const val COL_PERSON_YEAR_OF_ADMISSION = "year_of_admission"
        const val COL_PERSON_TYPE_SPECIALTY_ID = "specialty_id"

        // Таблица специальностей
        const val TABLE_SPECIALTY = "specialty"
        const val COL_SPECIALTY_ID = "id"
        const val COL_SPECIALTY_TITLE = "title"
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PERSONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SPECIALTY")
        onCreate(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_SPECIALTY (
                $COL_SPECIALTY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SPECIALTY_TITLE TEXT NOT NULL UNIQUE
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_PERSONS (
                $COL_PERSON_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PERSON_FIRST_NAME TEXT NOT NULL,
                $COL_PERSON_LAST_NAME TEXT NOT NULL,
                $COL_PERSON_YEAR INTEGER NOT NULL,
                $COL_PERSON_YEAR_OF_ADMISSION INTEGER NOT NULL,
                $COL_PERSON_TYPE_SPECIALTY_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_PERSON_TYPE_SPECIALTY_ID) 
                REFERENCES $TABLE_SPECIALTY($COL_SPECIALTY_ID)
            )
        """)
    }

    fun getAllPersons(): List<Person> {
        val db = readableDatabase
        val persons = mutableListOf<Person>()

        val query = """
            SELECT p.*, s.$COL_SPECIALTY_TITLE 
            FROM $TABLE_PERSONS p
            JOIN $TABLE_SPECIALTY s ON p.$COL_PERSON_TYPE_SPECIALTY_ID = s.$COL_SPECIALTY_ID
        """

        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                persons.add(
                    Person(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PERSON_ID)),
                        firstName = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERSON_FIRST_NAME)),
                        lastName = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERSON_LAST_NAME)),
                        year = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PERSON_YEAR)),
                        yearOfAdmission = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PERSON_YEAR_OF_ADMISSION)),
                        specialtyTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_SPECIALTY_TITLE))
                    )
                )
            }
        }
        return persons
    }


    // Метод для генерации тестовых данных
    fun generateData(count: Int, random: Random = Random.Default) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (i in 1..count) {
                // Генерация специальности
                val specialtyTitle = "Специальность-${UUID.randomUUID()}"
                val specialtyId = db.insert(TABLE_SPECIALTY, null,
                    ContentValues().apply {
                        put(COL_SPECIALTY_TITLE, specialtyTitle)
                    }
                )

                if (specialtyId != -1L) {
                    // Генерация персоны
                    db.insert(TABLE_PERSONS, null,
                        ContentValues().apply {
                            put(COL_PERSON_FIRST_NAME, "Имя $i")
                            put(COL_PERSON_LAST_NAME, "Фамилия $i")
                            put(COL_PERSON_YEAR, random.nextInt(1970, 2024))
                            put(COL_PERSON_YEAR_OF_ADMISSION, random.nextInt(2010, 2024))
                            put(COL_PERSON_TYPE_SPECIALTY_ID, specialtyId)
                        }
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Очистка всех данных
    fun clearAllData() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PERSONS, null, null)
            db.delete(TABLE_SPECIALTY, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}