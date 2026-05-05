package com.rohandas.taskmanagerpro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.data.model.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onStatusChange: ((Task) -> Unit)? = null,
    private val onDelete: ((Task) -> Unit)? = null
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val expandedStates = mutableMapOf<String, Boolean>()

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvTime: TextView = view.findViewById(R.id.tvTaskTime)
        val tvCategory: TextView = view.findViewById(R.id.tvTaskCategory)
        val cbStatus: CheckBox = view.findViewById(R.id.cbTaskStatus)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpandTask)
        val ivDelete: ImageView = view.findViewById(R.id.ivDeleteTask)
        val tvDescription: TextView = view.findViewById(R.id.tvTaskDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTitle.text = task.title
        holder.tvTime.text = "${task.date} | ${task.time}"
        holder.tvCategory.text = task.category
        holder.tvDescription.text = if (task.description.isEmpty()) "No description provided." else task.description

        // Checkbox status and Title style
        holder.cbStatus.setOnCheckedChangeListener(null)
        val isDone = task.status == "Done"
        holder.cbStatus.isChecked = isDone
        
        if (isDone) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTitle.setTextColor(holder.itemView.context.getColor(R.color.tp_text_secondary))
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTitle.setTextColor(holder.itemView.context.getColor(R.color.tp_text_primary))
        }

        holder.cbStatus.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) "Done" else "Ongoing"
            if (task.status != newStatus) {
                onStatusChange?.invoke(task.copy(status = newStatus))
            }
        }


        // Expansion logic
        val isExpanded = expandedStates[task.id] ?: false
        holder.tvDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivExpand.rotation = if (isExpanded) 180f else 0f

        holder.ivExpand.setOnClickListener {
            val nextState = !isExpanded
            expandedStates[task.id] = nextState
            notifyItemChanged(position)
        }

        holder.ivDelete.setOnClickListener {
            onDelete?.invoke(task)
        }

        holder.itemView.setOnLongClickListener {
            onDelete?.invoke(task)
            true
        }
    }

    override fun getItemCount() = tasks.size

    fun updateData(newTasks: List<Task>) {
        android.util.Log.d("TaskAdapter", "Updating data with ${newTasks.size} tasks")
        tasks = ArrayList(newTasks) // Create a fresh copy to ensure Diff/Equality checks pass
        notifyDataSetChanged()
    }
}

