package com.example.todoapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.data.Task
import com.example.todoapp.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: List<Task>,
    private val onStatusChange: (Task, String) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        with(holder.binding) {
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvCategory.text = "Categoría: ${task.category}"
            tvPriority.text = "Prioridad: ${task.priority}"
            tvStatus.text = task.status

            // Color según prioridad
            when (task.priority) {
                "Alta" -> tvPriority.setTextColor(Color.RED)
                "Media" -> tvPriority.setTextColor(Color.parseColor("#FF9800"))
                else -> tvPriority.setTextColor(Color.GREEN)
            }

            // Cambiar estados
            btnNextStatus.setOnClickListener {
                val nextStatus = when (task.status) {
                    "PENDIENTE" -> "EN_PROGRESO"
                    "EN_PROGRESO" -> "COMPLETADA"
                    else -> "PENDIENTE"
                }
                onStatusChange(task, nextStatus)
            }

            btnDelete.setOnClickListener { onDelete(task) }
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateData(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
