package com.example.music_app.fileTools

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.myapplication_3.ExpenseItem
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfContentByte

object PDFGeneratorExpense {

    fun generatePdf(context: Context, expenses: List<ExpenseItem>) { // Исправлено имя параметра
        try {
            val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "expenses")
            if (!pdfDirectory.exists()) {
                pdfDirectory.mkdirs()
            }

            val pdfFile = File(pdfDirectory, "expense_list.pdf")

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD) // Жирный шрифт для заголовка
            document.add(Phrase("Список Расходов", titleFont)) // Русский заголовок
            document.add(Chunk.NEWLINE)


            for (expense in expenses) { // Исправлено имя переменной и цикл
                document.add(Phrase("Expense: ${expense.expense}\nDate: ${expense.date}\nType: ${expense.type}\n")) // Русские названия полей
                document.add(Chunk.NEWLINE)
            }

            document.close()

            println("PDF File saved at: ${pdfFile.absolutePath}")

        } catch (e: Exception) {
            e.printStackTrace() //  Лучше использовать логирование: Log.e("PDFGenerator", "Ошибка генерации PDF", e)
        }
    }


    fun getPdfFilePath(context: Context): String? { // Возвращаем null, если файл не существует
        val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "expenses") // Исправлено имя директории
        val pdfFile = File(pdfDirectory, "expense_list.pdf") // Исправлено имя файла
        return if (pdfFile.exists()) pdfFile.absolutePath else null
    }
}