package com.example.myapplication_3.expense

import android.content.Context
import android.util.Log
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

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            val expense = getCellValue(row.getCell(0)).toDoubleOrNull() ?: continue
            val date = getCellValue(row.getCell(1))
            val type = getCellValue(row.getCell(2))

            expenses.add(ExpenseItem(expense, date, type))
        }
        return expenses
    }

    fun addLineToXLS(expenseItem: ExpenseItem) {
        val sheet = workbook?.getSheetAt(0) ?: return
        val newRow = sheet.createRow(sheet.lastRowNum + 1)
        newRow.createCell(0).setCellValue(expenseItem.expense)
        newRow.createCell(1).setCellValue(expenseItem.date)
        newRow.createCell(2).setCellValue(expenseItem.type)
        saveWorkbook()
    }

    fun updateLineInXLS(oldLine: String, newLine: String) {
        val sheet = workbook?.getSheetAt(0) ?: return
        for (row in sheet) {
            val cell = row.getCell(0)
            if (cell?.stringCellValue == oldLine) {
                val parts = newLine.split(",")
                parts.forEachIndexed { colIndex, part ->
                    val newCell = row.createCell(colIndex)
                    newCell.setCellValue(part)
                }
                saveWorkbook()
                return
            }
        }
    }

    fun deleteLineFromXLS(line: String) {
        val sheet = workbook?.getSheetAt(0) ?: return
        val rowIndex = sheet.iterator().asSequence().toList()
            .indexOfFirst { it.getCell(0)?.stringCellValue == line }

        if (rowIndex >= 0) {
            sheet.removeRow(sheet.getRow(rowIndex)) // Удаляем строку напрямую

            // Сдвигаем оставшиеся строки вверх
            if (rowIndex < sheet.lastRowNum) {
                sheet.shiftRows(rowIndex + 1, sheet.lastRowNum, -1)
            }

            // Удаляем последнюю строку, если она пустая после сдвига
            val lastRow = sheet.getRow(sheet.lastRowNum)
            if (lastRow == null || lastRow.physicalNumberOfCells == 0) {
                sheet.removeRow(lastRow)
            }

            saveWorkbook()
        }
    }

    private fun copyRowData(sourceRow: Row?, destinationRow: Row) {
        if (sourceRow == null) return
        for (cellIndex in 0 until sourceRow.physicalNumberOfCells) {
            val sourceCell = sourceRow.getCell(cellIndex)
            val destinationCell = destinationRow.createCell(cellIndex)
            copyCellValue(sourceCell, destinationCell)
        }
    }


    private fun copyCellValue(source: Cell?, destination: Cell) {
        if (source == null) return
        when (source.cellType) {
            CellType.STRING -> destination.setCellValue(source.stringCellValue)
            CellType.NUMERIC -> destination.setCellValue(source.numericCellValue)
            CellType.BOOLEAN -> destination.setCellValue(source.booleanCellValue)
            else -> destination.setCellValue("") // Для других типов данных
        }
    }




    private fun getCellValue(cell: Cell?): String {
        return cell?.toString() ?: ""
    }

}