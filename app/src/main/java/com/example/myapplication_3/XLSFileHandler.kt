package com.example.myapplication_3.fileTools

import android.content.Context
import android.util.Log
import com.example.myapplication_3.ExpenseItem
import com.example.myapplication_3.IncomeItem
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object XLSFileHandler {
//    private const val XLS_FILE_NAME = "expenses_data.xls" // Имя файла
    private var xlsFile: File? = null
    private var workbook: Workbook? = null

    fun initialize(context: Context, fileName: String) {
        xlsFile = File(context.filesDir, fileName)
        Log.d("XLSFileHandler", "XLS File Path: ${xlsFile?.absolutePath}")
        if (!xlsFile!!.exists()) {
            createWorkbook()
        } else {
            loadWorkbook()
        }

        // Добавляем initialExpense, если его нет в файле
        val sheet = workbook!!.getSheetAt(0)
        if (sheet.lastRowNum == 0) { // Проверяем, есть ли данные (кроме заголовка)
            val initialExpense = ExpenseItem(0.0, "", "")
            val row = sheet.createRow(1)
            row.createCell(0).setCellValue(initialExpense.expense)
            row.createCell(1).setCellValue(initialExpense.date)
            row.createCell(2).setCellValue(initialExpense.type)
            saveWorkbook()
        }
    }

    private fun createWorkbook() {
        workbook = HSSFWorkbook()
        val sheet = workbook!!.createSheet("Expenses")

        saveWorkbook()
    }

    private fun createSheet(sheetName: String) {
        val sheet = workbook!!.createSheet(sheetName)
        val headerRow = sheet.createRow(0)
        // Заголовки столбцов
        headerRow.createCell(0).setCellValue("Expense") // Общий заголовок для суммы/расхода
        headerRow.createCell(1).setCellValue("Date")
        headerRow.createCell(2).setCellValue("Type")
    }

    private fun saveWorkbook() { try {
        FileOutputStream(xlsFile).use { fileOut ->
            workbook?.write(fileOut)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    }

    private fun loadWorkbook() { try {
        FileInputStream(xlsFile).use { fileIn ->
            workbook = HSSFWorkbook(fileIn)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    }

    fun addLineToXLS(expenseItem: ExpenseItem) {
        val sheet = workbook?.getSheetAt(0) ?: return
        val newRow = sheet.createRow(sheet.lastRowNum + 1)
        newRow.createCell(0).setCellValue(expenseItem.expense)
        newRow.createCell(1).setCellValue(expenseItem.date)
        newRow.createCell(2).setCellValue(expenseItem.type)
        saveWorkbook()
    }



    fun saveExpensesData(context: Context, expenseList: List<ExpenseItem>){
        saveDataToXLS(expenseList) // Правильно вызываем saveDataToXLS
        saveWorkbook()

    }

    private fun saveDataToXLS(data: List<ExpenseItem>) { // Принимаем List<ExpenseItem>
        val sheet = workbook?.getSheetAt(0) ?: return

        // Удаление предыдущих данных (можно оставить, если нужно)
        val lastRowNum = sheet.lastRowNum
        if (lastRowNum > 0) {
            for (i in lastRowNum downTo 1) {
                sheet.removeRow(sheet.getRow(i))
            }
        }

        // Запись новых данных
        data.forEachIndexed { index, expenseItem ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(expenseItem.expense)
            row.createCell(1).setCellValue(expenseItem.date)
            row.createCell(2).setCellValue(expenseItem.type)
        }

        saveWorkbook()
    }


    fun updateLineInXLS(oldExpense: ExpenseItem, newExpense: ExpenseItem) {
        val sheet = workbook?.getSheetAt(0) ?: return
        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            if (row != null) {
                val expense = getCellValue(row.getCell(0)).toDoubleOrNull()
                val date = getCellValue(row.getCell(1))
                val type = getCellValue(row.getCell(2))
                if(expense != null) {
                    val currentExpense = ExpenseItem(expense, date, type)

                    if (currentExpense == oldExpense) {
                        row.getCell(0).setCellValue(newExpense.expense)
                        row.getCell(1).setCellValue(newExpense.date)
                        row.getCell(2).setCellValue(newExpense.type)
                        saveWorkbook()
                        return
                    }
                }
            }
        }
    }


    fun deleteLineFromXLS(expenseItem: ExpenseItem) {
        val sheet = workbook?.getSheetAt(0) ?: return

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            if (row != null) {
                val expense = getCellValue(row.getCell(0)).toDoubleOrNull()
                val date = getCellValue(row.getCell(1))
                val type = getCellValue(row.getCell(2))
                if (expense != null) {
                    val currentExpense = ExpenseItem(expense, date, type)

                    if (currentExpense == expenseItem) {
                        sheet.removeRow(sheet.getRow(rowIndex))
                        if (rowIndex < sheet.lastRowNum) {
                            sheet.shiftRows(rowIndex+1, sheet.lastRowNum, -1)
                        }
                        saveWorkbook()
                        return
                    }
                }
            }
        }

    }




    fun loadDataFromXLS(): List<ExpenseItem> {
        val sheet = workbook?.getSheetAt(0) ?: return emptyList()
        val expenses = mutableListOf<ExpenseItem>()

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            if (row != null) {
                try {
                    val expense = row.getCell(0).numericCellValue.toDouble()
                    val date = row.getCell(1)?.stringCellValue ?: ""
                    val type = row.getCell(2)?.stringCellValue ?: ""
                    expenses.add(ExpenseItem(expense, date, type))

                } catch (e: Exception) {
                    // Обработка ошибок чтения ячеек (например, если ячейка не того типа)
                    // Логирование или другие действия
                }

            }
        }
        return expenses
    }



    private fun getCellValue(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue ?: ""
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }

    }


}