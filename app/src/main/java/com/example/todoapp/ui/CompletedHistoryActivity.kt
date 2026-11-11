package com.example.todoapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.viewmodel.TaskViewModel

class CompletedHistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_history)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val prefs = getSharedPreferences("app_session", MODE_PRIVATE)
        val userId = prefs.getInt("LOGGED_USER_ID", -1)
        viewModel.activeUserId = userId

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

        viewModel.loadCompletedTasks()
    }
}