package com.example.myapplication_3

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(private val expenseItems: MutableList<String>) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>(){

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
        if (expenseItems.isEmpty()) {
            expenseItems.add(expense)
            notifyItemInserted(0)
        } else {
            expenseItems[0] = expense
            notifyItemChanged(0)
        }
    }
}