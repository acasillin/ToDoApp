package com.example.todoapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CompletedHistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_history)

        preferencesManager = PreferencesManager(this)
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val rvCompleted = findViewById<RecyclerView>(R.id.rvCompleted)

        adapter = TaskAdapter(
            tasks = emptyList(),
            onStatusChange = { task, newStatus ->
                val updated = task.copy(status = newStatus)
                viewModel.updateTask(updated)
            },
            onDelete = { task ->
                viewModel.deleteTask(task.id)
            }
        )

        rvCompleted.layoutManager = LinearLayoutManager(this)
        rvCompleted.adapter = adapter

        viewModel.completedTasks.observe(this) { list ->
            adapter.updateData(list)
        }

        //  DataStore antes de cargar el historial
        lifecycleScope.launch {
            val savedUserId = preferencesManager.activeUserIdFlow.first()
            viewModel.activeUserId = savedUserId
            viewModel.loadCompletedTasks()
        }
    }
}