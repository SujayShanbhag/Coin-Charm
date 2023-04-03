package com.courage.CoinCharm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.courage.notifications.R
import com.courage.CoinCharm.model.Expense

class ExpenseRecyclerAdapter(val context : Context,var itemList : ArrayList<Expense>)
    : RecyclerView.Adapter<ExpenseRecyclerAdapter.ExpenseViewHolder>() {
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val place: TextView = view.findViewById(R.id.txtPlace)
        val date: TextView = view.findViewById(R.id.txtDate)
        val amount: TextView = view.findViewById(R.id.txtAmount)
        val item: RelativeLayout = view.findViewById(R.id.item)
    }

    fun setFilteredList(filteredList : ArrayList<Expense>) {
        this.itemList=filteredList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_single_expense_row, parent, false)

        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = itemList[position]
        holder.place.text = expense.place
        holder.date.text = expense.date
        holder.amount.text = expense.amount
        if(expense.sign){
            val color=ContextCompat.getColor(context,R.color.green)
            holder.amount.setTextColor(color)
        }
        else {
            val color=ContextCompat.getColor(context,R.color.red)
            holder.amount.setTextColor(color)
        }
    }
}