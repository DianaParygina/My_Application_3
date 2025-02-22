package com.example.myapplication_3.BD

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PersonDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "person_db", null, 6) {

    companion object {
        const val TABLE_PERSONS = "person"
        const val COL_PERSON_ID = "_id"
        const val COL_PERSON_FIRST_NAME = "first_name"
        const val COL_PERSON_LAST_NAME = "last_name"
        const val COL_PERSON_YEAR = "year"
        const val COL_PERSON_YEAR_OF_ADMISSION = "year_of_admission"
        const val COL_PERSON_TYPE_SPECIALTY_ID = "specialty_id"

        const val TABLE_SPECIALTY = "specialty"
        const val COL_SPECIALTY_ID = "_id"
        const val COL_SPECIALTY_TITLE = "title"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlPersons = """
            CREATE TABLE $TABLE_PERSONS (
                $COL_PERSON_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PERSON_FIRST_NAME TEXT NOT NULL,
                $COL_PERSON_LAST_NAME TEXT NOT NULL,
                $COL_PERSON_YEAR INTEGER NOT NULL,  
                $COL_PERSON_YEAR_OF_ADMISSION INTEGER NOT NULL,
                $COL_PERSON_TYPE_SPECIALTY_ID INTEGER NOT NULL, 
                FOREIGN KEY ($COL_PERSON_TYPE_SPECIALTY_ID) REFERENCES $TABLE_SPECIALTY($COL_SPECIALTY_ID)
            )
        """

        val sqlISpecialty = """   
            CREATE TABLE $TABLE_SPECIALTY (
                $COL_SPECIALTY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SPECIALTY_TITLE TEXT NOT NULL UNIQUE
            )
        """

        db.execSQL(sqlISpecialty)
        db.execSQL(sqlPersons)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PERSONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SPECIALTY")
        onCreate(db)
    }

    fun clearData(db: SQLiteDatabase) {
        db.delete(TABLE_PERSONS, null, null)
        db.delete(TABLE_SPECIALTY, null, null)
    }
}