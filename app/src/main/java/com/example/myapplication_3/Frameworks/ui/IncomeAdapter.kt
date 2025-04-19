package com.example.myapplication_3.Frameworks.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.R
//import SharedFinanceViewModel
import com.example.myapplication_3.Frameworks.database.IncomeDatabaseHelper
import com.example.myapplication_3.Entities.Income
import com.example.myapplication_3.Controllers.SharedFinanceViewModel
import com.example.myapplication_3.Frameworks.files.BinFileHandler
import java.text.SimpleDateFormat
import java.util.*

class IncomeAdapter(
    val incomes: MutableList<Income>,
    private val sharedFinanceViewModel: SharedFinanceViewModel,
    private val fragment: IncomeFragment,
    private val dbHelper: IncomeDatabaseHelper
) : RecyclerView.Adapter<IncomeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val textViewIncome: TextView = view.findViewById(R.id.textViewIncome)
        val textViewData: TextView = view.findViewById(R.id.textViewIncomeDate)
        val textViewType: TextView = view.findViewById(R.id.textViewIncomeType)

        init {
//            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_income, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val incomeItem = incomes[position]
        viewHolder.textViewIncome.text = incomeItem.amount.toString()
        viewHolder.textViewData.text = incomeItem.date
        viewHolder.textViewType.text = incomeItem.type

        if (incomes.isNotEmpty()) {
            viewHolder.itemView.setOnLongClickListener { view ->
                fragment.showContextMenuForItem(position, view)
                true
            }
        }
    }

    override fun getItemCount() = incomes.size

    fun addIncome(income: Income) {
        incomes.add(0, income)
        notifyItemInserted(0)

//        if (fragment.useSql) {
//        } else {
            BinFileHandler.addLineToBin(income)
//        }
    }

    fun deleteIncome(position: Int) {
        val income = incomes[position]
        if (fragment.useSql) {
            income.id?.let { incomeId ->
                dbHelper.deleteIncome(incomeId)
                sharedFinanceViewModel.deleteIncome(income)
            } ?: showToast("Ошибка при удалении: id дохода null")
        } else {
            BinFileHandler.deleteLineFromBin(income)
            sharedFinanceViewModel.deleteIncome(income)
        }
        incomes.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateIncome(position: Int, updatedIncome: Income) {
        if (fragment.useSql) {
            updatedIncome.id?.let { dbHelper.updateIncome(updatedIncome, it) }
        } else {
            BinFileHandler.updateLineInBin(incomes[position], updatedIncome)
        }

        val oldAmount = incomes[position]
        sharedFinanceViewModel.deleteIncome(oldAmount)
        sharedFinanceViewModel.addIncome(
            updatedIncome.amount - oldAmount.amount,
            updatedIncome.date,
            updatedIncome.type
        )

        incomes[position] = updatedIncome
        notifyItemChanged(position)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = 0 // Без drag & drop
                val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN // Разрешаем свайпы вверх и вниз
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (direction == ItemTouchHelper.DOWN) {
                    deleteIncome(position)
                } else if (direction == ItemTouchHelper.UP) {
                    showEditIncomeDialog(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun showEditIncomeDialog(position: Int) {
        val context = fragment.requireContext() // Используем контекст фрагмента
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Редактировать доход")

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_income, null)
        builder.setView(view)

        val inputIncome = view.findViewById<EditText>(R.id.input_amount)
        val inputDate = view.findViewById<EditText>(R.id.input_date)
        val spinner = view.findViewById<Spinner>(R.id.income_type_spinner)

        val currentIncome = incomes[position]
        inputIncome.setText(currentIncome.amount.toString())
        inputDate.setText(currentIncome.date)

        // Настройка DatePicker
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            inputDate.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time))
        }

        inputDate.setOnClickListener {
            DatePickerDialog(
                context,
                datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Настройка Spinner
        val incomeTypes = dbHelper.getAllIncomeTypes()
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            incomeTypes.map { it.name }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        // Установка текущего типа
        incomeTypes.indexOfFirst { it.name == currentIncome.type }.takeIf { it != -1 }?.let {
            spinner.setSelection(it)
        }

        builder.setPositiveButton("OK") { _, _ ->
            val newAmount = inputIncome.text.toString().toDoubleOrNull()
            val newDate = inputDate.text.toString()
            val selectedType = incomeTypes[spinner.selectedItemPosition]

            when {
                newAmount == null -> showToast("Введите корректное число")
                newDate.isEmpty() -> showToast("Заполните дату")
                else -> {
                    val updatedIncome = Income(
                        currentIncome.id,
                        newAmount,
                        newDate,
                        selectedType.name
                    )

                    if (fragment.useSql) {
                        currentIncome.id?.let { incomeId ->
                            dbHelper.updateIncome(updatedIncome, incomeId)
                        } ?: showToast("Ошибка: id дохода не найден")
                    } else {
                        BinFileHandler.updateLineInBin(currentIncome, updatedIncome)
                    }

                    // Обновляем данные
                    incomes[position] = updatedIncome
                    notifyItemChanged(position)

                    // Обновляем общую сумму
                    val difference = newAmount - currentIncome.amount
                    sharedFinanceViewModel.addIncome(
                        newAmount - currentIncome.amount,  // amount (difference)
                        updatedIncome.date,                 // date
                        updatedIncome.type                  // type
                    )

                    showToast("Доход обновлен. Текущий доход: ${sharedFinanceViewModel.getTotalIncome()} руб")
                }
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(fragment.requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}