package com.example.myapplication_3.frameworks.files

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.myapplication_3.R
import com.example.myapplication_3.entities.ExpenseItem
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PDFGeneratorExpense {

    fun generateAndShowPdf(context: Context, expenses: List<ExpenseItem>) {
        try {
            generatePdf(context, expenses)
            openPdf(context)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.pdf_generation_error), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun generatePdf(context: Context, expenses: List<ExpenseItem>) {
        val pdfDirectory = getPdfDirectory(context)
        val pdfFile = File(pdfDirectory, "expense_list.pdf")

        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))

        document.open()

        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        document.add(Phrase("Expense List\n", titleFont))
        document.add(Chunk.NEWLINE)

        for (expense in expenses) {
            document.add(Phrase("Expense: ${expense.expense}\nDate: ${expense.date}\nType: ${expense.type}\n"))
            document.add(Chunk.NEWLINE)
        }
        document.close()
    }

    private fun openPdf(context: Context) {
        getPdfFile(context)?.let { pdfFile ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(Intent.createChooser(this, context.getString(R.string.open_pdf)))
            }
        } ?: Toast.makeText(context, context.getString(R.string.pdf_not_found_error), Toast.LENGTH_SHORT).show()
    }

    private fun getPdfDirectory(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "expenses").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getPdfFile(context: Context): File? {
        val pdfFile = File(getPdfDirectory(context), "expense_list.pdf")
        return if (pdfFile.exists()) pdfFile else null
    }
}