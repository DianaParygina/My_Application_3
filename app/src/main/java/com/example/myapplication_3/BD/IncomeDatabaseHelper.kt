package com.example.myapplication_3.BD

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication_3.income.Income
import com.example.myapplication_3.income.IncomeType

class IncomeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "finance_db", null, 4) { // Увеличиваем версию БД

    companion object {
        const val TABLE_INCOMES = "incomes"
        const val COL_INCOME_ID = "_id"
        const val COL_INCOME_AMOUNT = "amount"
        const val COL_INCOME_DATE = "date"
        const val COL_INCOME_TYPE_ID = "type_id" // !!! Ссылаемся на id типа дохода

        const val TABLE_INCOME_TYPES = "income_types"  // !!! Новая таблица для типов доходов
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

        db.execSQL(sqlIncomeTypes)    // !!! Сначала создаем таблицу типов
        db.execSQL(sqlIncomes)
        Log.d("IncomeDatabaseHelper", "Database created")

        // !!! Добавляем несколько начальных типов доходов
        insertIncomeType(db, "Зарплата")
        insertIncomeType(db, "Подработка")
        insertIncomeType(db, "Подарок")
        insertIncomeType(db, "Дивиденды")

    }

    // !!! Функция для добавления типа дохода (можно использовать в onCreate и в приложении)
    fun insertIncomeType(db: SQLiteDatabase, typeName: String): Long {
        val values = ContentValues().apply {
            put(COL_TYPE_NAME, typeName)
        }
        return db.insert(TABLE_INCOME_TYPES, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOME_TYPES") // !!! Удаляем таблицу типов
        onCreate(db)
    }


    // !!! Изменена функция добавления дохода. Теперь она принимает id типа дохода.
    fun insertIncome(amount: Double, date: String, typeId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_INCOME_AMOUNT, amount)
            put(COL_INCOME_DATE, date)
            put(COL_INCOME_TYPE_ID, typeId) // !!! Используем id типа
        }
        val newRowId = db.insert(TABLE_INCOMES, null, values)
        db.close()
        return newRowId
    }

    // !!!  Получаем все доходы с названиями типов
    fun getAllIncomes(): List<Income> {
        val db = readableDatabase
        val incomes = mutableListOf<Income>()

        val sql = """
            SELECT i.*, t.$COL_TYPE_NAME 
            FROM $TABLE_INCOMES i
            JOIN $TABLE_INCOME_TYPES t ON i.$COL_INCOME_TYPE_ID = t.$COL_TYPE_ID
            ORDER BY i.$COL_INCOME_ID DESC
        """
        val cursor = db.rawQuery(sql, null)

        with(cursor) {
            while (moveToNext()) {
                val income = Income(
                    getLong(getColumnIndexOrThrow(COL_INCOME_ID)),
                    getDouble(getColumnIndexOrThrow(COL_INCOME_AMOUNT)),
                    getString(getColumnIndexOrThrow(COL_INCOME_DATE)),
                    getString(getColumnIndexOrThrow(COL_TYPE_NAME)) // !!! Получаем имя типа
                )
                incomes.add(income)
            }
            close()
        }

        db.close()
        return incomes
    }



    // !!!  Получаем все типы доходов
    fun getAllIncomeTypes(): List<IncomeType> {
        val db = readableDatabase
        val incomeTypes = mutableListOf<IncomeType>()

        val cursor = db.query(
            TABLE_INCOME_TYPES,
            null,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val incomeType = IncomeType(
                    getInt(getColumnIndexOrThrow(COL_TYPE_ID)),
                    getString(getColumnIndexOrThrow(COL_TYPE_NAME))
                )
                incomeTypes.add(incomeType)
            }
            close()
        }

        db.close()
        return incomeTypes
    }



    private fun getIncomeTypeIdByName(typeName: String): Long? { // !!! Новый метод
        val db = readableDatabase
        val cursor = db.query(
            TABLE_INCOME_TYPES,
            arrayOf(COL_TYPE_ID),
            "$COL_TYPE_NAME = ?",
            arrayOf(typeName),
            null, null, null
        )
        var typeId: Long? = null
        with(cursor) {
            if (moveToFirst()) {
                typeId = getLong(getColumnIndexOrThrow(COL_TYPE_ID))
            }
            close()
        }
        db.close()
        return typeId
    }


    fun updateIncome(income: Income, incomeId: Long) {
        val db = writableDatabase
        val typeId = getIncomeTypeIdByName(income.type) // Получаем ID типа по имени

        if (typeId != null) {
            val values = ContentValues().apply {
                put(COL_INCOME_AMOUNT, income.amount)
                put(COL_INCOME_DATE, income.date)
                put(COL_INCOME_TYPE_ID, typeId) // Используем ID типа
            }
            db.update(TABLE_INCOMES, values, "$COL_INCOME_ID = ?", arrayOf(incomeId.toString()))
        } else {
            // Обработать ошибку: тип не найден
            Log.e("IncomeDatabaseHelper", "Type not found for name: ${income.type}")
            // Или показать Toast с ошибкой
        }
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
                    getLong(getColumnIndexOrThrow(COL_INCOME_TYPE_ID)).let { typeId ->
                        getIncomeTypeNameById(typeId) ?: "Unknown Type" // !!! Получаем имя типа по id
                    }
                )
            }
            close()
        }

        db.close()
        return income
    }



    private fun getIncomeTypeNameById(typeId: Long): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_INCOME_TYPES,
            arrayOf(COL_TYPE_NAME), // Выбираем только имя типа
            "$COL_TYPE_ID = ?",
            arrayOf(typeId.toString()),
            null, null, null
        )
        var typeName: String? = null
        with(cursor) {
            if (moveToFirst()) {
                typeName = getString(getColumnIndexOrThrow(COL_TYPE_NAME))
            }
            close()
        }
        db.close()
        return typeName
    }

}