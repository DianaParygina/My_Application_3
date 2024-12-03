package com.example.myapplication_3.income

import android.content.Context
import android.os.Environment
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PDFGeneratorIncome {

    fun generatePdf(context: Context, incomes: List<IncomeItem>) { // Исправлено имя параметра
        try {
            val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "incomes")
            if (!pdfDirectory.exists()) {
                pdfDirectory.mkdirs()
            }

            val pdfFile = File(pdfDirectory, "income_list.pdf")

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD) // Жирный шрифт для заголовка
            document.add(Phrase("List income\n", titleFont)) // Русский заголовок
            document.add(Chunk.NEWLINE)


            for (income in incomes) { // Исправлено имя переменной и цикл
                document.add(Phrase("Income: ${income.amount}\nDate: ${income.date}\nType: ${income.type}\n")) // Русские названия полей
                document.add(Chunk.NEWLINE)
            }

            document.close()

            println("PDF File saved at: ${pdfFile.absolutePath}")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getPdfFilePath(context: Context): String? { // Возвращаем null, если файл не существует
        val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "incomes") // Исправлено имя директории
        val pdfFile = File(pdfDirectory, "income_list.pdf") // Исправлено имя файла
        return if (pdfFile.exists()) pdfFile.absolutePath else null
    }

}