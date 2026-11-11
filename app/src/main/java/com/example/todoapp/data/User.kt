package com.example.todoapp.data

data class User(
    val userId: Int = 0,
    val username: String,
    val passwordHash: String
)