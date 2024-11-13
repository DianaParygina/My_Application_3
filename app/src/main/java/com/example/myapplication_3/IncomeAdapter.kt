package com.example.myapplication_3

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class IncomeAdapter(val incomeItems: MutableList<String>, private val sharedFinanceViewModel: SharedFinanceViewModel, private val activity: IncomeActivity) : RecyclerView.Adapter<IncomeAdapter.ViewHolder>(){

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

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                deleteIncome(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun getIncomesList(): List<String> {
        return incomeItems
    }
}