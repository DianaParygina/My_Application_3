package com.example.myapplication_3.income

import android.app.AlertDialog
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

class IncomeAdapter(
    val incomeItems: MutableList<IncomeItem>,
    private val sharedFinanceViewModel: SharedFinanceViewModel,
    private val activity: IncomeActivity
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
        val incomeItem = incomeItems[position]
        viewHolder.textViewIncome.text = incomeItem.amount.toString()
        viewHolder.textViewData.text = incomeItem.date
        viewHolder.textViewType.text = incomeItem.type

        viewHolder.itemView.setOnLongClickListener { view ->
            activity.showContextMenuForItem(position, view)
            true
        }
    }

    override fun getItemCount() = incomeItems.size

    fun addIncome(income: IncomeItem) {
        incomeItems.add(0, income)
        notifyItemInserted(0)
    }

    fun deleteIncome(position: Int) {
        val incomeItem = incomeItems[position]

        // Удаляем из базы данных
        if (activity.useSql) {
            // Получаем ID дохода для удаления
            val incomeId = incomeItem.amount.toLong() // Здесь нужно использовать правильный ID
            activity.dbHelper.deleteIncome(incomeId)
        } else {
            BinFileHandler.deleteLineFromBin(incomeItem)
        }

        // Удаляем из адаптера
        incomeItems.removeAt(position)
        notifyItemRemoved(position)
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

        val currentIncomeItem = incomeItems[position]
        inputIncome.setText(currentIncomeItem.amount.toString())
        inputDate.setText(currentIncomeItem.date)
        inputType.setText(currentIncomeItem.type)

        builder.setPositiveButton("OK") { _, _ ->
            val newIncomeString = inputIncome.text.toString()
            val newDate = inputDate.text.toString()
            val newType = inputType.text.toString()

            if (newIncomeString.isNotEmpty()) {
                val newIncome = newIncomeString.toDoubleOrNull()
                if (newIncome != null) {
                    // Обновляем в базе данных
                    if (activity.useSql) {
                        // Получаем ID для обновления
                        val incomeId = currentIncomeItem.amount.toLong() // Здесь нужно использовать правильный ID
                        activity.dbHelper.updateIncome(IncomeItem(newIncome, newDate, newType), incomeId)
                    }

                    // Обновляем элемент в адаптере
                    val newIncomeItem = IncomeItem(newIncome, newDate, newType)
                    incomeItems[position] = newIncomeItem
                    notifyItemChanged(position)
                    showToast("Ваш доход ${sharedFinanceViewModel.getTotalIncome()} руб")
                } else {
                    showToast("Введите корректное число")
                }
            } else {
                showToast("Введите сумму дохода")
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
