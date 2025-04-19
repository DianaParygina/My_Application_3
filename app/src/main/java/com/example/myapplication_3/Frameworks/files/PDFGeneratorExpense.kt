package com.example.myapplication_3.Frameworks.files

import android.content.Context
import android.os.Environment
import com.example.myapplication_3.Entities.ExpenseItem
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PDFGeneratorExpense {

    fun generatePdf(context: Context, expenses: List<ExpenseItem>) {
        try {
            val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "expenses")
            if (!pdfDirectory.exists()) {
                pdfDirectory.mkdirs()
            }

            val pdfFile = File(pdfDirectory, "expense_list.pdf")

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
            document.add(Phrase("List expens\n", titleFont))
            document.add(Chunk.NEWLINE)


            for (expense in expenses) {
                document.add(Phrase("Expense: ${expense.expense}\nDate: ${expense.date}\nType: ${expense.type}\n"))
                document.add(Chunk.NEWLINE)
            }

            document.close()

            println("PDF File saved at: ${pdfFile.absolutePath}")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getPdfFilePath(context: Context): String? { // Возвращаем null, если файл не существует
        val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "expenses")
        val pdfFile = File(pdfDirectory, "expense_list.pdf")
        return if (pdfFile.exists()) pdfFile.absolutePath else null
    }
}