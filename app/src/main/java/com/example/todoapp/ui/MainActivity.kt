package com.example.todoapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.data.Task
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.databinding.DialogAddTaskBinding
import com.example.todoapp.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar ViewModel (Compatibilidad estándar)
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        // Obtener usuario en sesión
        val prefs = getSharedPreferences("app_session", MODE_PRIVATE)
        val userId = prefs.getInt("LOGGED_USER_ID", -1)

        if (userId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        viewModel.activeUserId = userId
        setupRecyclerView()

        // Observar lista de tareas
        viewModel.activeTasks.observe(this) { taskList ->
            adapter.updateData(taskList)
        }

        // Búsqueda en tiempo real
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterQuery = s.toString().trim()
                viewModel.loadActiveTasks()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Botones de la interfaz
        binding.btnAddTask.setOnClickListener { showAddTaskDialog() }

        binding.btnGoHistory.setOnClickListener {
            startActivity(Intent(this, CompletedHistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.loadActiveTasks()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            tasks = emptyList(),
            onStatusChange = { task, newStatus ->
                val updatedTask = task.copy(status = newStatus)
                viewModel.updateTask(updatedTask)
            },
            onDelete = { task ->
                viewModel.deleteTask(task.id)
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { dialog, _ ->
                val title = dialogBinding.etTitle.text.toString().trim()
                val desc = dialogBinding.etDescription.text.toString().trim()
                val category = dialogBinding.spCategory.selectedItem.toString()
                val priority = dialogBinding.spPriority.selectedItem.toString()

                if (title.isNotEmpty()) {
                    viewModel.addTask(title, desc, category, priority, "PENDIENTE")
                } else {
                    Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadActiveTasks()
    }
}