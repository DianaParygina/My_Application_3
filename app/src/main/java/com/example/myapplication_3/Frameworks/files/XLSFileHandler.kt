package com.example.myapplication_3.Frameworks.files

import android.content.Context
import android.util.Log
import com.example.myapplication_3.Entities.ExpenseItem
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object XLSFileHandler {
    private var xlsFile: File? = null
    private var workbook: Workbook? = null

    fun initialize(context: Context, fileName: String) {
        xlsFile = File(context.filesDir, fileName)
        Log.d("XLSFileHandler", "XLS File Path: ${xlsFile?.absolutePath}")
        if (!xlsFile!!.exists()) {
            workbook = HSSFWorkbook()
            val sheet = workbook!!.createSheet("Expenses")

            // Создаем строку заголовков
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Expense")
            headerRow.createCell(1).setCellValue("Data")
            headerRow.createCell(2).setCellValue("Type")

            saveWorkbook()

        } else {
            loadWorkbook()
        }
    }

    private fun saveWorkbook() {
        try {
            FileOutputStream(xlsFile).use { fileOut ->
                workbook?.write(fileOut)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun loadWorkbook() {
        try {
            FileInputStream(xlsFile).use { fileIn ->
                workbook = HSSFWorkbook(fileIn)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun loadDataFromXLS(): List<ExpenseItem> {
        val sheet = workbook?.getSheetAt(0) ?: return emptyList()
        val expenses = mutableListOf<ExpenseItem>()

        // Начинаем с 1, чтобы пропустить строку заголовка
        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            // Извлекаем значения из ячеек, обрабатывая null и конвертируя типы
            val expense = getCellValue(row.getCell(0)).toDoubleOrNull() ?: continue
            val date = getCellValue(row.getCell(1))
            val type = getCellValue(row.getCell(2))

            expenses.add(ExpenseItem(expense, date, type))
        }
        return expenses
    }

    private fun getCellValue(cell: Cell?): String {
        // Более надежная обработка значений ячеек
        return when (cell?.cellType) {
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.STRING -> cell.stringCellValue
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> cell.cellFormula
            CellType.BLANK -> ""
            CellType._NONE -> ""
            CellType.ERROR -> cell.errorCellValue.toString()
            null -> ""
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

    fun updateLineInXLS(position: Int, newLine: ExpenseItem) {
        val sheet = workbook?.getSheetAt(0) ?: return
        val row = sheet.getRow(position + 1) ?: return

        row.getCell(0)?.setCellValue(newLine.expense)
        row.getCell(1)?.setCellValue(newLine.date)
        row.getCell(2)?.setCellValue(newLine.type)

        saveWorkbook()
    }

    fun deleteLineFromXLS(expenseItem: ExpenseItem) {
        val sheet = workbook?.getSheetAt(0) ?: return

        if (sheet.physicalNumberOfRows == 0) return // Проверка на пустой лист

        for (rowIndex in 1..sheet.lastRowNum) { // Итерация по строкам, начиная с 1, пропуская заголовок
            val row = sheet.getRow(rowIndex) ?: continue // Пропустить пустую строку

            val expense = getCellValue(row.getCell(0)).toDoubleOrNull()
            val date = getCellValue(row.getCell(1))
            val type = getCellValue(row.getCell(2))

            if (expense == expenseItem.expense && date == expenseItem.date && type == expenseItem.type) {
                sheet.removeRow(row) // Удаляем найденную строку

                // Сдвигаем строки вверх, чтобы заполнить пробел
                if (rowIndex < sheet.lastRowNum) {
                    sheet.shiftRows(rowIndex + 1, sheet.lastRowNum + 1, -1)
                }
                saveWorkbook()
                return
            }
        }
    }

    fun saveDataToXLS(data: List<String>) {
        val sheet = workbook?.getSheetAt(0) ?: return

        // Удаляем все строки кроме заголовка
        val lastRowNum = sheet.lastRowNum
        if (lastRowNum > 0) {
            for (i in lastRowNum downTo 1) {
                val row = sheet.getRow(i)
                if (row != null) {
                    sheet.removeRow(row)
                }
            }
        }

        // Записываем новые данные
        data.forEachIndexed { index, line ->
            val row = sheet.createRow(index + 1)  // Индекс с 1, чтобы не перезаписать заголовок
            val parts = line.split(",")
            parts.forEachIndexed { colIndex, part ->
                val cell = row.createCell(colIndex)
                cell.setCellValue(part)
            }
        }

        saveWorkbook()
    }

}