package com.example.to_doapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.logging.Logger

class TaskRecyclerViewAdapter(private val taskList: List<TaskItem>, private val listener: OnItemClickListener) : RecyclerView.Adapter<TaskRecyclerViewAdapter.TaskViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(taskItem: TaskItem)
        fun onEditClick(taskItem: TaskItem)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTaskName: TextView = itemView.findViewById(R.id.textViewTaskName)
        val textViewTaskDescription: TextView = itemView.findViewById(R.id.textViewTaskDescription)
        val imageViewDeleteTask: ImageView = itemView.findViewById(R.id.imageViewDeleteTask)
        val imageViewEditTask: ImageView = itemView.findViewById(R.id.imageViewEditTask)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.task_item_layout, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem = taskList[position]

        holder.textViewTaskName.text = currentItem.taskName
        holder.textViewTaskDescription.visibility = if (currentItem.expanded) View.VISIBLE else View.GONE
        holder.textViewTaskDescription.text = currentItem.taskDescription

        holder.imageViewDeleteTask.visibility = if (currentItem.expanded) View.VISIBLE else View.GONE
        holder.imageViewEditTask.visibility = if (currentItem.expanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            currentItem.expanded = !currentItem.expanded
            notifyItemChanged(position)
        }

        holder.imageViewDeleteTask.setOnClickListener {
            listener.onDeleteClick(currentItem)
        }

        holder.imageViewEditTask.setOnClickListener {
            listener.onEditClick(currentItem)
        }

    }


    override fun getItemCount(): Int {
        return taskList.size
    }
}

