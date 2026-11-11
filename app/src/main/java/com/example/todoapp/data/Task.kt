package com.example.todoapp.data

data class Task(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val category: String, // La categoria de la tarea ( "Escuela", "Trabajo", "Personal")
    val priority: String, // ("Alta", "Media", "Baja")
    val status: String    // ("PENDIENTE", "EN_PROGRESO", "COMPLETADA")
)