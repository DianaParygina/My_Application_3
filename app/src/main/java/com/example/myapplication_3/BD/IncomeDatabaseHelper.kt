package com.example.myapplication_3.BD

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication_3.income.Income
import com.example.myapplication_3.income.IncomeType

class IncomeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "finance_db", null, 10) {

    companion object {
        const val TABLE_INCOMES = "incomes"
        const val COL_INCOME_ID = "_id"
        const val COL_INCOME_AMOUNT = "amount"
        const val COL_INCOME_DATE = "date"
        const val COL_INCOME_TYPE_ID = "type_id"

        const val TABLE_INCOME_TYPES = "income_types"
        const val COL_TYPE_ID = "_id"
        const val COL_TYPE_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlIncomes = """
            CREATE TABLE $TABLE_INCOMES (
                $COL_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INCOME_AMOUNT REAL NOT NULL,
                $COL_INCOME_DATE TEXT NOT NULL,
                $COL_INCOME_TYPE_ID INTEGER NOT NULL,  
                FOREIGN KEY ($COL_INCOME_TYPE_ID) REFERENCES $TABLE_INCOME_TYPES($COL_TYPE_ID)
            )
        """

        val sqlIncomeTypes = """   
            CREATE TABLE $TABLE_INCOME_TYPES (
                $COL_TYPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TYPE_NAME TEXT NOT NULL UNIQUE    
            )
        """

        db.execSQL(sqlIncomeTypes)
        db.execSQL(sqlIncomes)
        Log.d("IncomeDatabaseHelper", "Database created")

        insertIncomeType(db, "Zp")
        insertIncomeType(db, "Present")

    }

    fun insertIncomeType(db: SQLiteDatabase, typeName: String): Long {
        val values = ContentValues().apply {
            put(COL_TYPE_NAME, typeName)
        }
        return db.insert(TABLE_INCOME_TYPES, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOME_TYPES")
        onCreate(db)
    }


    fun insertIncome(amount: Double, date: String, typeId: Long): Long {
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COL_INCOME_AMOUNT, amount)
                put(COL_INCOME_DATE, date)
                put(COL_INCOME_TYPE_ID, typeId)
            }
            db.insert(TABLE_INCOMES, null, values)
        }
    }

    fun getAllIncomes(): List<Income> {
        return readableDatabase.use { db ->
            val incomes = mutableListOf<Income>()
            val sql = """
            SELECT i.*, t.$COL_TYPE_NAME 
            FROM $TABLE_INCOMES i
            JOIN $TABLE_INCOME_TYPES t ON i.$COL_INCOME_TYPE_ID = t.$COL_TYPE_ID
            ORDER BY i.$COL_INCOME_ID DESC
        """
            db.rawQuery(sql, null).use { cursor ->
                with(cursor) {
                    while (moveToNext()) {
                        val income = Income(
                            getLong(getColumnIndexOrThrow(COL_INCOME_ID)),
                            getDouble(getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                            getString(getColumnIndexOrThrow(COL_INCOME_DATE)),
                            getString(getColumnIndexOrThrow(COL_TYPE_NAME))
                        )
                        incomes.add(income)
                    }
                }
            }
            incomes
        }
    }



    fun getAllIncomeTypes(): List<IncomeType> {
        return readableDatabase.use { db ->
            val incomeTypes = mutableListOf<IncomeType>()
            db.query(
                TABLE_INCOME_TYPES,
                null, null, null, null, null, null
            ).use { cursor ->
                with(cursor) {
                    while (moveToNext()) {
                        val incomeType = IncomeType(
                            getInt(getColumnIndexOrThrow(COL_TYPE_ID)),
                            getString(getColumnIndexOrThrow(COL_TYPE_NAME))
                        )
                        incomeTypes.add(incomeType)
                    }
                }
            }
            incomeTypes
        }
    }



    fun getIncomeTypeIdByName(db: SQLiteDatabase, typeName: String): Long? {
        return db.query(
            TABLE_INCOME_TYPES,
            arrayOf(COL_TYPE_ID),
            "$COL_TYPE_NAME = ?",
            arrayOf(typeName),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_TYPE_ID))
            } else {
                null
            }
            }
    }



    fun updateIncome(income: Income, incomeId: Long) {
        writableDatabase.use { db ->
            val typeId = getIncomeTypeIdByName(db, income.type)

            if (typeId != null) {
                val values = ContentValues().apply {
                    put(COL_INCOME_AMOUNT, income.amount)
                    put(COL_INCOME_DATE, income.date)
                    put(COL_INCOME_TYPE_ID, typeId)
                }
                db.update(TABLE_INCOMES, values, "$COL_INCOME_ID = ?", arrayOf(incomeId.toString()))
            } else {
                Log.e("IncomeDatabaseHelper", "Type not found for name: ${income.type}")
            }
        }
    }

    fun deleteIncome(incomeId: Long) {
        writableDatabase.use { db ->
            db.delete(TABLE_INCOMES, "$COL_INCOME_ID = ?", arrayOf(incomeId.toString()))
        }
    }

    fun getIncomeById(incomeId: Long): Income? {
        return readableDatabase.use { db ->
            db.query(
                TABLE_INCOMES,
                null,
                "$COL_INCOME_ID = ?",
                arrayOf(incomeId.toString()),
                null, null, null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val typeId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INCOME_TYPE_ID))
                    Income(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_INCOME_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_INCOME_DATE)),
                        getIncomeTypeNameById(db, typeId) ?: "Unknown Type"
                    )
                } else {
                    null
                }
            }
        }
    }



    private fun getIncomeTypeNameById(db: SQLiteDatabase, typeId: Long): String? {
        return db.query(
            TABLE_INCOME_TYPES,
            arrayOf(COL_TYPE_NAME),
            "$COL_TYPE_ID = ?",
            arrayOf(typeId.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE_NAME))
            } else {
                null
            }
        }
    }
}