package com.example.myapplication_3.BD

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication_3.income.Income

class IncomeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "finance_db", null, 3) {

    companion object {
        const val TABLE_INCOMES = "incomes"
        const val COL_INCOME_ID = "_id"
        const val COL_INCOME_AMOUNT = "amount"
        const val COL_INCOME_DATE = "date"
        const val COL_INCOME_TYPE = "type"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlIncomes = """
            CREATE TABLE $TABLE_INCOMES (
                $COL_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INCOME_AMOUNT REAL NOT NULL,
                $COL_INCOME_DATE TEXT NOT NULL,
                $COL_INCOME_TYPE TEXT NOT NULL
            )
        """

        db.execSQL(sqlIncomes)
        Log.d("IncomeDatabaseHelper", "Database created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOMES")
        onCreate(db)
    }

    fun insertIncome(amount: Double, date: String, type: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_INCOME_AMOUNT, amount)
            put(COL_INCOME_DATE, date)
            put(COL_INCOME_TYPE, type)
        }
        val newRowId = db.insert(TABLE_INCOMES, null, values)
        db.close()
        return newRowId
    }

    fun getAllIncomes(): List<Income> {
        val db = readableDatabase
        val incomes = mutableListOf<Income>()

        val cursor = db.query(
            TABLE_INCOMES,
            null,
            null,
            null,
            null,
            null,
            "$COL_INCOME_ID DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val income = Income(
                    getLong(getColumnIndexOrThrow(COL_INCOME_ID)),
                    getDouble(getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                    getString(getColumnIndexOrThrow(COL_INCOME_DATE)),
                    getString(getColumnIndexOrThrow(COL_INCOME_TYPE))
                )
                incomes.add(income)
            }
            close()
        }

        db.close()
        return incomes
    }

    fun updateIncome(income: Income, incomeId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_INCOME_AMOUNT, income.amount)
            put(COL_INCOME_DATE, income.date)
            put(COL_INCOME_TYPE, income.type)
        }
        db.update(TABLE_INCOMES, values, "$COL_INCOME_ID = ?", arrayOf(incomeId.toString()))
        db.close()
    }

    fun deleteIncome(incomeId: Long) {
        val db = writableDatabase
        db.delete(TABLE_INCOMES, "$COL_INCOME_ID = ?", arrayOf(incomeId.toString()))
        db.close()
    }

    fun getIncomeById(incomeId: Long): Income? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_INCOMES,
            null,
            "$COL_INCOME_ID = ?",
            arrayOf(incomeId.toString()),
            null,
            null,
            null
        )

        var income: Income? = null
        with(cursor) {
            if (moveToFirst()) {
                income = Income(
                    getLong(getColumnIndexOrThrow(COL_INCOME_ID)),
                    getDouble(getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                    getString(getColumnIndexOrThrow(COL_INCOME_DATE)),
                    getString(getColumnIndexOrThrow(COL_INCOME_TYPE))
                )
            }
            close()
        }

        db.close()
        return income
    }

}