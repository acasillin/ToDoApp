package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.DatabaseHelper
import com.example.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    val activeTasks = MutableLiveData<List<Task>>()
    val completedTasks = MutableLiveData<List<Task>>()

    var activeUserId: Int = -1
    var filterQuery: String = ""
    var filterStatus: String? = null
    var filterCategory: String? = null

    fun loadActiveTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = dbHelper.getFilteredTasks(
                userId = activeUserId,
                query = filterQuery,
                status = filterStatus,
                category = filterCategory
            )
            activeTasks.postValue(list)
        }
    }

    fun loadCompletedTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = dbHelper.getCompletedTasks(activeUserId)
            completedTasks.postValue(list)
        }
    }

    fun addTask(title: String, desc: String, category: String, priority: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = Task(
                userId = activeUserId,
                title = title,
                description = desc,
                category = category,
                priority = priority,
                status = status
            )
            dbHelper.insertTask(task)
            loadActiveTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.updateTask(task)
            loadActiveTasks()
            loadCompletedTasks()
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteTask(taskId)
            loadActiveTasks()
            loadCompletedTasks()
        }
    }
}