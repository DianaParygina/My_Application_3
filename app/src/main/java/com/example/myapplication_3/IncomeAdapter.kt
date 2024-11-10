package com.example.myapplication_3

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncomeAdapter(private val incomeItems: MutableList<String>) : RecyclerView.Adapter<IncomeAdapter.ViewHolder>(){

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
        if(incomeItems.isEmpty()) {
            incomeItems.add(income)
            notifyItemInserted(0)
        }else{
            incomeItems[0] = income
            notifyItemChanged(0)
        }
    }
}