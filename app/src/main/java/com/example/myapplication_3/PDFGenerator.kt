//package com.example.myapplication_3.fileTools
//
//import android.content.Context
//import android.os.Build
//import android.os.Environment
//import android.provider.DocumentsContract
//import androidx.annotation.RequiresApi
//import com.example.myapplication_3.ExpenseItem
//import com.example.myapplication_3.IncomeItem
//import com.itextpdf.text.Document
//import com.itextpdf.text.Font
//import com.itextpdf.text.Phrase
//import com.itextpdf.text.Chunk
//import com.itextpdf.text.pdf.PdfWriter
//
//import java.io.File
//import java.io.FileOutputStream
//
//
////@RequiresApi(Build.VERSION_CODES.Q)
//object PDFGenerator {
//
//    fun generatePdf(context: Context, incomeList: List<IncomeItem>, expenseList: List<ExpenseItem>) {
//        try {
//            val pdfDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "finance") // Папка "finance"
//            if (!pdfDirectory.exists()) {
//                pdfDirectory.mkdirs()
//            }
//
//            val pdfFile = File(pdfDirectory, "finance_report.pdf") // Имя файла
//
//            val document = Document()
//            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
//            document.open()
//
//            // Добавляем секцию для доходов
//            addSectionToDocument(document, "Income", incomeList)
//
//            // Добавляем секцию для расходов
//            addSectionToDocument(document, "Expenses", expenseList)
//
//            document.close()
//
//            println("PDF File saved at: ${pdfFile.absolutePath}")
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun addSectionToDocument(document: Document, title: String, items: List<Any>) { // List<Any> для общности
//        document.add(Phrase(title, Font(Font.FontFamily.HELVETICA, 16f)))
//        document.add(Chunk.NEWLINE)
//
//        items.forEach { item ->
//            when (item) {
//                is IncomeItem -> {
//                    document.add(Phrase("Amount: ${item.amount}, Date: ${item.date}, Type: ${item.type}\n"))
//                }
//                is ExpenseItem -> {
//                    document.add(Phrase("Expense: ${item.expense}, Date: ${item.date}, Type: ${item.type}\n"))
//                }
//            }
//        }
//        document.add(Chunk.NEWLINE) // Разделяем секции пустой строкой
//    }
//
//
//    fun getPdfFilePath(context: Context): String? {  // Возвращает null, если файл не существует
//        val pdfDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.let { File(it, "finance") }
//        return if (pdfDirectory?.exists() == true) {
//            pdfDirectory.absolutePath + "/finance_report.pdf"
//        } else null
//    }
//}