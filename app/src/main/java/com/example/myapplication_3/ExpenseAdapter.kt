package com.example.myapplication_3

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
import kotlin.math.exp

class ExpenseAdapter(val expenseItems: MutableList<String>, private val sharedFinanceViewModel: SharedFinanceViewModel, private val activity: MainActivity, private val sharedPrefs: SharedPreferences) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.textViewExpense)
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

        viewHolder.textView.text = expenseItems[position]
    }

    override fun getItemCount() = expenseItems.size

    fun addExpense(expense: String) {
            expenseItems.add(0, expense)
            notifyItemInserted(0)
    }


    fun deleteExpense(position: Int) {
        val expenseString = expenseItems[position].replace("руб", "").trim()
        val expense = expenseString.toDoubleOrNull()
        if (expense != null) {
            sharedFinanceViewModel.deleteExpense(expense)
            expenseItems.removeAt(position)
            notifyItemRemoved(position)
        }

        val updatedExpenses = expenseItems.joinToString(",")
        with (activity.sharedPrefs.edit()) {
            putString("expenseList", updatedExpenses)
            apply()
        }

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

    fun getExpenseList(): List<String> {
        return expenseItems
    }






    fun showEditExpenseDialog(position: Int){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Редактировать расход")

        val input = EditText(activity)

        val currentExpenseString = expenseItems[position].replace("руб", "").trim()
        input.setText(currentExpenseString)

        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newExpenseString = input.text.toString()
            if (newExpenseString.isNotEmpty()) {
                val newExpense = newExpenseString.toDoubleOrNull()
                if (newExpense!= null) {
                    val oldExpense = currentExpenseString.toDoubleOrNull()
                    if(oldExpense!=null) {
                        sharedFinanceViewModel.deleteExpense(oldExpense)
                        sharedFinanceViewModel.addExpense(newExpense)
                        expenseItems[position] = newExpense.toString()
                        notifyItemChanged(position)
                        val updatedExpenses = expenseItems.joinToString(",")
                        with(sharedPrefs.edit()) {
                            putString("expenseList", updatedExpenses)
                            apply()
                        }
                    }
                    showToast("Ваш расход ${sharedFinanceViewModel.getTotalIncome()} руб")
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