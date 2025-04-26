package com.example.myapplication_3.frameworks.files

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.myapplication_3.R
import com.example.myapplication_3.entities.Income
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PDFGeneratorIncome {

    fun generateAndShowPdf(context: Context, incomes: List<Income>) {
        try {
            generatePdf(context, incomes)
            openPdf(context)
        } catch (e: Exception) {
            showToast(context, context.getString(R.string.pdf_generation_error))
            e.printStackTrace()
        }
    }

    private fun generatePdf(context: Context, incomes: List<Income>) {
        val pdfDirectory = getPdfDirectory(context)
        val pdfFile = File(pdfDirectory, "income_list.pdf")

        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))

        document.open()

        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        document.add(Phrase("Income List\n", titleFont))
        document.add(Chunk.NEWLINE)

        for (income in incomes) {
            document.add(Phrase("Amount: ${income.amount}\nDate: ${income.date}\nType: ${income.type}\n"))
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
        } ?: showToast(context, context.getString(R.string.pdf_not_found_error))
    }

    private fun getPdfDirectory(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "incomes").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getPdfFile(context: Context): File? {
        val pdfFile = File(getPdfDirectory(context), "income_list.pdf")
        return if (pdfFile.exists()) pdfFile else null
    }

    private fun showToast(context: Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}