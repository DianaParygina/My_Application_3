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

class IncomeAdapter(val incomeItems: MutableList<String>, private val sharedFinanceViewModel: SharedFinanceViewModel, private val activity: IncomeActivity, private val sharedPrefs: SharedPreferences) : RecyclerView.Adapter<IncomeAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.textViewIncome)
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_income, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView.text = incomeItems[position]
    }

    override fun getItemCount() = incomeItems.size


    fun addIncome(income: String){
            incomeItems.add(0,income)
            notifyItemInserted(0)
    }


    fun deleteIncome(position: Int) {
        val incomeString = incomeItems[position].replace("руб", "").trim()
        val income = incomeString.toDoubleOrNull()
        if (income != null) {
            sharedFinanceViewModel.deleteIncome(income)
            incomeItems.removeAt(position)
            notifyItemRemoved(position)
        }

        val updatedIncome = incomeItems.joinToString(",")
        with (activity.sharedPrefs.edit()) {
            putString("incomeList", updatedIncome)
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
                    deleteIncome(position)
                }else if (direction == ItemTouchHelper.UP){
                    showEditIncomeDialog(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun getIncomesList(): List<String> {
        return incomeItems
    }


    fun showEditIncomeDialog(position: Int){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Редактировать доход")

        val input = EditText(activity)

        val currentIncomeString = incomeItems[position].replace("руб", "").trim()
        input.setText(currentIncomeString)

        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newIncomeString = input.text.toString()
            if (newIncomeString.isNotEmpty()) {
                val newIncome = newIncomeString.toDoubleOrNull()
                if (newIncome!= null) {
                    val oldIncome = currentIncomeString.toDoubleOrNull()
                    if(oldIncome!=null) {
                        sharedFinanceViewModel.deleteIncome(oldIncome)
                        sharedFinanceViewModel.addIncome(newIncome)
                        incomeItems[position] = newIncome.toString()
                        notifyItemChanged(position)
                        val updatedIncome = incomeItems.joinToString(",")
                        with(sharedPrefs.edit()) {
                            putString("incomeList", updatedIncome)
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