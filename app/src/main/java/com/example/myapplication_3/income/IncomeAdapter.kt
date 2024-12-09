package com.example.myapplication_3.income

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.R
import com.example.myapplication_3.SharedFinanceViewModel
import com.example.myapplication_3.BD.IncomeDatabaseHelper
import com.example.myapplication_3.income.BinFileHandler.addLineToBin
import com.example.myapplication_3.income.BinFileHandler.deleteLineFromBin
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IncomeAdapter(
    val incomes: MutableList<Income>,
    private val sharedFinanceViewModel: SharedFinanceViewModel,
    private val activity: IncomeActivity,
    private val dbHelper: IncomeDatabaseHelper
) : RecyclerView.Adapter<IncomeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val textViewIncome: TextView = view.findViewById(R.id.textViewIncome)
        val textViewData: TextView = view.findViewById(R.id.textViewIncomeDate)
        val textViewType: TextView = view.findViewById(R.id.textViewIncomeType)

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            // Здесь можно добавить элементы контекстного меню, если нужно
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

        viewHolder.itemView.setOnLongClickListener { view ->
            activity.showContextMenuForItem(position, view)
            true
        }
    }

    override fun getItemCount() = incomes.size

    fun addIncome(income: Income) { // Принимает Income
        if (activity.useSql) {
            val newId = activity.dbHelper.insertIncome(income.amount, income.date, income.type)
            incomes.add(0, income.copy(id = newId)) // Копируем с новым id
        } else {
            BinFileHandler.addLineToBin(income)
            incomes.add(0, income)
        }
        notifyItemInserted(0)
    }

    fun deleteIncome(position: Int) {
        val income = incomes[position]
        val amountToDelete = income.amount // !!! Сохраняем сумму для удаления

        if (activity.useSql) {
            income.id?.let {
                activity.dbHelper.deleteIncome(it)
                sharedFinanceViewModel.deleteIncome(amountToDelete) // !!! Вызываем после удаления из БД
            }
        } else {
            BinFileHandler.deleteLineFromBin(income)
            sharedFinanceViewModel.deleteIncome(amountToDelete) // !!! Вызываем после удаления из файла
        }

        incomes.removeAt(position)
        notifyItemRemoved(position)
    }


    fun updateIncome(position: Int, updatedIncome: Income) {
        if (activity.useSql) {
            updatedIncome.id?.let { activity.dbHelper.updateIncome(updatedIncome, it) }
        } else {
            val oldIncome = incomes[position] // Получаем старый доход
            BinFileHandler.updateLineInBin(oldIncome, updatedIncome) // Обновляем в файле
        }
        incomes[position] = updatedIncome
        notifyItemChanged(position)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
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
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Редактировать доход")
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_income, null)
        builder.setView(view)

        val inputIncome = view.findViewById<EditText>(R.id.input_amount)
        val inputDate = view.findViewById<EditText>(R.id.input_date)
        val inputType = view.findViewById<EditText>(R.id.input_type)

        val currentIncome = incomes[position]
        inputIncome.setText(currentIncome.amount.toString())
        inputDate.setText(currentIncome.date)
        inputType.setText(currentIncome.type)

        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            inputDate.setText(format.format(calendar.time))
        }

        inputDate.setOnClickListener {
            DatePickerDialog(
                activity,
                datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        builder.setPositiveButton("OK") { _, _ ->
            val newIncomeString = inputIncome.text.toString()
            val newDate = inputDate.text.toString()
            val newType = inputType.text.toString()

            if (newIncomeString.isNotEmpty() && newDate.isNotEmpty() && newType.isNotEmpty()) {
                val newAmount = newIncomeString.toDoubleOrNull()

                if (newAmount != null) {
                    if (activity.useSql) {
                        val incomeId = currentIncome.id
                        if (incomeId != null) {
                            val updatedIncome = Income(incomeId, newAmount, newDate, newType)
                            activity.dbHelper.updateIncome(updatedIncome, incomeId)
                            incomes[position] = updatedIncome
                            notifyItemChanged(position)
                            showToast("Доход обновлен")

                            sharedFinanceViewModel.deleteIncome(currentIncome.amount)
                            sharedFinanceViewModel.addIncome(newAmount)

                        } else {
                            showToast("Ошибка: id дохода не найден")
                        }
                    } else {
                        val oldIncome = incomes[position]
                        val newIncome = Income(null, newAmount, newDate, newType)
                        BinFileHandler.updateLineInBin(oldIncome, newIncome)
                        incomes[position] = newIncome
                        notifyItemChanged(position)

                        sharedFinanceViewModel.deleteIncome(currentIncome.amount)
                        sharedFinanceViewModel.addIncome(newAmount)

                        showToast("Доход обновлен")
                    }
                    val oldAmount = currentIncome.amount
                    val difference = newAmount - oldAmount
                    sharedFinanceViewModel.addIncome(difference)
                    showToast("Ваш доход ${sharedFinanceViewModel.getTotalIncome()} руб") // Исправлено сообщение

                } else {
                    showToast("Введите корректное число")
                }
            } else {
                showToast("Заполните все поля")
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
