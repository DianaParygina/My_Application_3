package com.example.myapplication_3.expense

import android.app.AlertDialog
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.R
import com.example.myapplication_3.SharedFinanceViewModel
import com.example.myapplication_3.income.IncomeAdapter.ViewHolder

class ExpenseAdapter(
    val expenseItems: MutableList<ExpenseItem>,
    private val sharedFinanceViewModel: SharedFinanceViewModel,
    private val fragment: ExpenseFragment
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val textViewExpense: TextView = view.findViewById(R.id.textViewExpense)
        val textViewType: TextView = view.findViewById(R.id.textViewExpenseType)
        val textViewDate: TextView = view.findViewById(R.id.textViewExpenseDate)

        init {
//            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_expense, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val expenseItem = expenseItems[position]
        viewHolder.textViewExpense.text = expenseItem.expense.toString()
        viewHolder.textViewDate.text = expenseItem.date
        viewHolder.textViewType.text = expenseItem.type

        if (expenseItems.isNotEmpty()) {
            viewHolder.itemView.setOnLongClickListener { view ->
                fragment.showContextMenuForItem(position, view)
                true
            }
        }
    }

    override fun getItemCount() = expenseItems.size

    fun addExpense(expense: ExpenseItem) {
        expenseItems.add(0, expense)
        notifyItemInserted(0)
    }

    fun deleteExpense(position: Int) {
        if (position in 0 until expenseItems.size) {
            val item = expenseItems[position]
            sharedFinanceViewModel.deleteExpense(item.expense)
            XLSFileHandler.deleteLineFromXLS(item)
            expenseItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun showEditExpenseDialog(position: Int) {
        if (position !in 0 until expenseItems.size) return

        val context = fragment.requireContext()
        val currentItem = expenseItems[position]

        AlertDialog.Builder(context).apply {
            setTitle("Редактировать расход")
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_expense, null)
            setView(view)

            view.findViewById<EditText>(R.id.input_expense).setText(currentItem.expense.toString())
            view.findViewById<EditText>(R.id.input_date_expense).setText(currentItem.date)
            view.findViewById<EditText>(R.id.input_type_expense).setText(currentItem.type)

            setPositiveButton("OK") { _, _ ->
                val expense = view.findViewById<EditText>(R.id.input_expense).text.toString().toDoubleOrNull()
                val date = view.findViewById<EditText>(R.id.input_date_expense).text.toString()
                val type = view.findViewById<EditText>(R.id.input_type_expense).text.toString()

                if (expense != null && date.isNotEmpty() && type.isNotEmpty()) {
                    val newItem = ExpenseItem(expense, date, type)
                    sharedFinanceViewModel.deleteExpense(currentItem.expense)
                    sharedFinanceViewModel.addExpense(expense)
                    XLSFileHandler.updateLineInXLS(position, newItem)
                    expenseItems[position] = newItem
                    notifyItemChanged(position)
                    Toast.makeText(context, "Расход обновлен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Заполните все поля корректно", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        }.show()
    }
}