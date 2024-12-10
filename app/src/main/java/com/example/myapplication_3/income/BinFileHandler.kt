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

    fun loadDataFromBin(): List<Income> {
        val incomes = mutableListOf<Income>()
        try {
            dataInputStream = DataInputStream(FileInputStream(binFile))
            while (dataInputStream!!.available() > 0) {
                val amount = dataInputStream!!.readDouble()
                val date = dataInputStream!!.readUTF()
                val type = dataInputStream!!.readUTF()
                val income = Income(
                    null, // id = null для бинарных файлов
                    amount,
                    date,
                    type
                )
                incomes.add(0,income)
            }
        } catch (e: EOFException) {
            // Достигнут конец файла
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataInputStream?.close()
        }
        return incomes
    }


    fun addLineToBin(incomeItem: Income) {
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

    fun deleteLineFromBin(incomeItem: Income) {

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


    fun updateLineInBin(oldIncome: Income, newIncome: Income) {
        deleteLineFromBin(oldIncome)
        addLineToBin(newIncome)
    }



    fun saveDataToBin(incomes: List<Income>) {
        try {
            dataOutputStream = DataOutputStream(FileOutputStream(binFile, false))
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