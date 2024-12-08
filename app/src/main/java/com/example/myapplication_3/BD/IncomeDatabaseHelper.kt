package com.example.myapplication_3.BD

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication_3.income.IncomeType

class IncomeDatabaseHelper(context: Context): SQLiteOpenHelper(context, "finance_db", null, 1) {

    companion object {
        // Названия таблиц и столбцов
        const val TABLE_TYPES = "types"
        const val COL_TYPE_ID = "_id"
        const val COL_TYPE_NAME = "name"

        const val TABLE_INCOMES = "incomes"
        const val COL_INCOME_ID = "_id"
        const val COL_INCOME_AMOUNT = "amount"
        const val COL_INCOME_DATE = "date"
        const val COL_INCOME_TYPE_ID = "type_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCategories = """
            CREATE TABLE $TABLE_TYPES (
                $COL_TYPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TYPE_NAME TEXT UNIQUE NOT NULL
            )
        """

        val sqlIncomes = """
            CREATE TABLE $TABLE_INCOMES (
                $COL_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INCOME_AMOUNT REAL NOT NULL,
                $COL_INCOME_DATE TEXT NOT NULL,
                $COL_INCOME_TYPE_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_INCOME_TYPE_ID) REFERENCES $TABLE_TYPES($COL_TYPE_ID)
            )
        """

        db.execSQL(sqlCategories)
        db.execSQL(sqlIncomes)
        Log.d("FinanceDatabaseHelper", "Database created")
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TYPES")
        onCreate(db)
    }

    fun insertIncome(amount: Double, date: String, type: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_INCOME_AMOUNT, amount)
            put(COL_INCOME_DATE, date)
            put(COL_INCOME_TYPE_ID, type)
        }
        val newRowId = db.insert(TABLE_INCOMES, null, values)
        db.close()
        return newRowId
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

        val income = with(cursor) {
            if (moveToFirst()) {
                Income(
                    getLong(getColumnIndexOrThrow(COL_INCOME_ID)),
                    getDouble(getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                    getString(getColumnIndexOrThrow(COL_INCOME_DATE)),
                    getInt(getColumnIndexOrThrow(COL_INCOME_TYPE_ID))
                )
            } else {
                null
            }
        }

        cursor.close()
        db.close()
        return income
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
            "$COL_INCOME_DATE DESC"
        )


        with(cursor) {
            while (moveToNext()) {
                val income = Income(
                    getLong(getColumnIndexOrThrow(COL_INCOME_ID)),
                    getDouble(getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                    getString(getColumnIndexOrThrow(COL_INCOME_DATE)),
                    getInt(getColumnIndexOrThrow(COL_INCOME_TYPE_ID))
                )
                incomes.add(income)
            }
            close()
        }

        db.close()
        return incomes
    }

    fun updateIncome(income: Income){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_INCOME_AMOUNT, income.amount)
            put(COL_INCOME_DATE, income.date)
            put(COL_INCOME_TYPE_ID, income.typeId)
        }
        db.update(TABLE_INCOMES, values, "$COL_INCOME_ID = ?", arrayOf(income.id.toString()))
        db.close()
    }

    fun deleteIncome(incomeId: Long) {
        val db = writableDatabase
        db.delete(TABLE_INCOMES, "$COL_INCOME_ID = ?", arrayOf(incomeId.toString()))
        db.close()
    }

    data class Income(val id: Long, val amount: Double, val date: String, val typeId: Int)
    data class IncomeItem(val id: Int? = null, val amount: Double, val date: String, val type: String)


//    fun clearIncomesTable() {
//        val db = writableDatabase
//        db.delete(TABLE_INCOMES, null, null)
//        db.close()
//    }



    fun insertType(name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TYPE_NAME, name)
        }
        val newRowId = db.insert(TABLE_TYPES, null, values)
        db.close()
        return newRowId
    }

    fun getAllTypes(): List<IncomeType> {
        val db = readableDatabase
        val types = mutableListOf<IncomeType>()

        val cursor = db.query(
            TABLE_TYPES,
            null,
            null,
            null,
            null,
            null,
            null
        )


        with(cursor) {
            while (moveToNext()) {
                val type = IncomeType(
                    getInt(getColumnIndexOrThrow(COL_TYPE_ID)),
                    getString(getColumnIndexOrThrow(COL_TYPE_NAME))
                )
                types.add(type)
            }
            close()
        }

        db.close()
        return types
    }

    fun updateType(type: IncomeType){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TYPE_NAME, type.name)
        }
        db.update(TABLE_TYPES, values, "$COL_TYPE_ID = ?", arrayOf(type.id.toString()))
        db.close()
    }

    fun deleteType(typeId: Long) {
        val db = writableDatabase
        db.delete(TABLE_TYPES, "$COL_TYPE_ID = ?", arrayOf(typeId.toString()))
        db.close()
    }

    data class IncomeType(val id: Int, val name: String)
}