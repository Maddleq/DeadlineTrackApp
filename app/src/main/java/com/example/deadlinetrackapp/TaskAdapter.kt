package com.example.deadlinetrackapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val onClick: (TaskUi) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskVH>() {

    private val items = mutableListOf<TaskUi>()

    fun submitList(newItems: List<TaskUi>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): TaskUi = items[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskVH(view, onClick)
    }

    override fun onBindViewHolder(holder: TaskVH, position: Int) {
        val bgRes = if (position % 2 == 0) R.color.task_row_a else R.color.task_row_b
        holder.itemRoot.setBackgroundResource(bgRes)
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TaskVH(
        itemView: View,
        private val onClick: (TaskUi) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomer: TextView = itemView.findViewById(R.id.tvCustomer)
        val itemRoot: View = itemView.findViewById(R.id.itemRoot)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private var current: TaskUi? = null

        init {
            itemView.setOnClickListener { current?.let(onClick) }
        }

        fun bind(item: TaskUi) {
            current = item
            tvTitle.text = item.title

            val customer = item.customer.trim()
            if (customer.isBlank()) {
                tvCustomer.visibility = View.GONE
            } else {
                tvCustomer.visibility = View.VISIBLE
                tvCustomer.text = customer
            }
        }
    }

}
