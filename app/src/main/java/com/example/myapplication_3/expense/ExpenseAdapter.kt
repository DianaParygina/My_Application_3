package com.example.myapplication_3.expense

import android.app.AlertDialog
import android.content.SharedPreferences
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

class ExpenseAdapter(val expenseItems: MutableList<ExpenseItem>, private val sharedFinanceViewModel: SharedFinanceViewModel, private val activity: MainActivity, private val sharedPrefs: SharedPreferences) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val textViewExpense: TextView
        val textViewType: TextView
        val textViewDate: TextView

        init {
            textViewExpense = view.findViewById(R.id.textViewExpense)
            textViewType = view.findViewById(R.id.textViewExpenseType)
            textViewDate = view.findViewById(R.id.textViewExpenseDate)
            view.setOnCreateContextMenuListener(this)
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
        viewHolder.textViewType.text = expenseItem.type
        viewHolder.textViewDate.text = expenseItem.date

        viewHolder.itemView.setOnLongClickListener { view ->
            activity.showContextMenuForItem(position, view)
            true
        }
    }

    override fun getItemCount() = expenseItems.size

    fun addExpense(expense: ExpenseItem) {
        expenseItems.add(0, expense)
        notifyItemInserted(0)

        saveExpensesToSharedPrefs()
    }

    private fun saveExpensesToSharedPrefs() {
        val incomeStrings = expenseItems.map { "${it.expense };${ it.date};${it.type}" }
        with(sharedPrefs.edit()) {
            putString("expenseList", incomeStrings.joinToString(";"))
            apply()
        }
    }


    fun deleteExpense(position: Int) {
        val expenseItem = expenseItems[position]
        ExpenseRepository.deleteExpense(expenseItem)
        expenseItems.removeAt(position)
        notifyItemRemoved(position)

        saveExpensesToSharedPrefs()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if(direction == ItemTouchHelper.DOWN) {
                    deleteExpense(position)
                }else if (direction == ItemTouchHelper.UP){
                    showEditExpenseDialog(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }




    fun showEditExpenseDialog(position: Int){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Редактировать расход")

        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_expense, null)
        builder.setView(view)

        val inputExpense = view.findViewById<EditText>(R.id.input_expense)
        val inputDate = view.findViewById<EditText>(R.id.input_date_expense)
        val inputType = view.findViewById<EditText>(R.id.input_type_expense)

        val currentExpenseItem = expenseItems[position]
        inputExpense.setText(currentExpenseItem.expense.toString())
        inputDate.setText(currentExpenseItem.date)
        inputType.setText(currentExpenseItem.type)

        builder.setPositiveButton("OK") { _, _ ->
            val newExpenseString = inputExpense.text.toString()
            val newDate = inputDate.text.toString()
            val newType = inputType.text.toString()
            if (newExpenseString.isNotEmpty()) {
                val newExpense = newExpenseString.toDoubleOrNull()
                if (newExpense!= null) {
                    ExpenseRepository.deleteExpense(currentExpenseItem)
                        sharedFinanceViewModel.addExpense(newExpense)
                        expenseItems[position] = ExpenseItem(newExpense, newDate, newType)
                        notifyItemChanged(position)
                        saveExpensesToSharedPrefs()
                    showToast("Ваш расход ${sharedFinanceViewModel.getTotalBalance()} руб")
                } else {
                    showToast("Введите корректное число")
                }
            } else {
                showToast("Введите сумму расхода")
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}