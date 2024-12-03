package com.example.myapplication_3.income

import android.content.Context
import java.io.*

object BinFileHandler {
    private var binFile: File? = null
    private var dataOutputStream: DataOutputStream? = null
    private var dataInputStream: DataInputStream? = null


    fun initialize(context: Context, fileName: String) {
        binFile = File(context.filesDir, fileName)
        if (!binFile!!.exists()) {
            try {
                binFile!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun loadDataFromBin(): List<IncomeItem> {
        val incomes = mutableListOf<IncomeItem>()
        try {
            dataInputStream = DataInputStream(FileInputStream(binFile))
            while (dataInputStream!!.available() > 0) {
                val amount = dataInputStream!!.readDouble()
                val date = dataInputStream!!.readUTF()
                val type = dataInputStream!!.readUTF()
                incomes.add(IncomeItem(amount, date, type))
            }
        } catch (e: EOFException) {
            // Достигнут конец файла, это нормально
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataInputStream?.close()
        }
        return incomes
    }


    fun addLineToBin(incomeItem: IncomeItem) {
        try {
            dataOutputStream = DataOutputStream(FileOutputStream(binFile, true)) // true для добавления в конец файла
            dataOutputStream!!.writeDouble(incomeItem.amount)
            dataOutputStream!!.writeUTF(incomeItem.date)
            dataOutputStream!!.writeUTF(incomeItem.type)
            dataOutputStream!!.flush() // Очищаем буфер, чтобы данные были записаны на диск
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataOutputStream?.close()
        }
    }

    fun deleteLineFromBin(incomeItem: IncomeItem) {

        val tempFile = File(binFile!!.absolutePath + ".tmp")
        try {
            dataInputStream = DataInputStream(FileInputStream(binFile))
            dataOutputStream = DataOutputStream(FileOutputStream(tempFile))

            while (dataInputStream!!.available() > 0) {
                val amount = dataInputStream!!.readDouble()
                val date = dataInputStream!!.readUTF()
                val type = dataInputStream!!.readUTF()

                if (amount != incomeItem.amount || date != incomeItem.date || type != incomeItem.type) {
                    dataOutputStream!!.writeDouble(amount)
                    dataOutputStream!!.writeUTF(date)
                    dataOutputStream!!.writeUTF(type)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataInputStream?.close()
            dataOutputStream?.close()
        }

        binFile!!.delete()
        tempFile.renameTo(binFile)
    }


    fun updateLineInBin(oldIncome: IncomeItem, newIncome: IncomeItem) {
        deleteLineFromBin(oldIncome)
        addLineToBin(newIncome)
    }



    fun saveDataToBin(incomes: List<IncomeItem>) {
        try {
            dataOutputStream = DataOutputStream(FileOutputStream(binFile, false)) // false для перезаписи файла
            for (incomeItem in incomes) {
                dataOutputStream!!.writeDouble(incomeItem.amount)
                dataOutputStream!!.writeUTF(incomeItem.date)
                dataOutputStream!!.writeUTF(incomeItem.type)
            }
            dataOutputStream!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataOutputStream?.close()
        }
    }

}