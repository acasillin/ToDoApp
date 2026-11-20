package com.example.todoapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.databinding.DialogAddTaskBinding
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        setupRecyclerView()
        setupCategoryFilter()

        // Observar lista de tareas
        viewModel.activeTasks.observe(this) { taskList ->
            adapter.updateData(taskList)
        }

        // Cargar sesión, tema e idioma desde DataStore
        lifecycleScope.launch {
            val isLoggedIn = preferencesManager.isLoggedInFlow.first()
            val savedUserId = preferencesManager.activeUserIdFlow.first()
            val isDark = preferencesManager.isDarkThemeFlow.first()
            val lang = preferencesManager.languageFlow.first()

            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(lang)
            )

            if (!isLoggedIn || savedUserId == -1) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return@launch
            }

            viewModel.activeUserId = savedUserId
            viewModel.loadActiveTasks()
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

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                preferencesManager.saveSession(isLoggedIn = false, userId = -1)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
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

    private fun setupCategoryFilter() {
        val spinnerAdapter = DialogAddTaskBinding.inflate(layoutInflater).spCategory.adapter
        val categories = (0 until spinnerAdapter.count).map { index ->
            spinnerAdapter.getItem(index).toString()
        }
        val allOptions = listOf("Todas") + categories

        binding.chipGroupCategories.removeAllViews()

        allOptions.forEachIndexed { index, categoryName ->
            val chip = Chip(this).apply {
                text = categoryName
                isCheckable = true
                isChecked = (index == 0)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.filterCategory = if (categoryName == "Todas") null else categoryName
                        viewModel.loadActiveTasks()
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
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
        if (viewModel.activeUserId != -1) {
            viewModel.loadActiveTasks()
        }
    }
}