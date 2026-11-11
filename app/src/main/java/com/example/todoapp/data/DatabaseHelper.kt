package com.example.todoapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "todo_app.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE users (
                userId INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            )
        """.trimIndent()

        val createTasksTable = """
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                category TEXT NOT NULL,
                priority TEXT NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createTasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // --- SEGURIDAD Y AUTENTICACIÓN ---
    fun registerUser(username: String, pass: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", pass)
        }
        return db.insert("users", null, values)
    }

    fun loginUser(username: String, pass: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT userId FROM users WHERE username = ? AND password = ?",
            arrayOf(username, pass)
        )
        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }
        cursor.close()
        return userId
    }

    // --- OPERACIONES DE TAREAS ---
    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("userId", task.userId)
            put("title", task.title)
            put("description", task.description)
            put("category", task.category)
            put("priority", task.priority)
            put("status", task.status)
        }
        return db.insert("tasks", null, values)
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("category", task.category)
            put("priority", task.priority)
            put("status", task.status)
        }
        return db.update("tasks", values, "id = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(taskId: Int): Int {
        val db = writableDatabase
        return db.delete("tasks", "id = ?", arrayOf(taskId.toString()))
    }

    // Consulta filtrada para la pantalla principal (excluye las completadas si es necesario)
    fun getFilteredTasks(userId: Int, query: String, status: String?, category: String?): List<Task> {
        val list = mutableListOf<Task>()
        val db = readableDatabase

        val conditions = mutableListOf("userId = ? AND status != 'COMPLETADA'")
        val args = mutableListOf(userId.toString())

        if (query.isNotEmpty()) {
            conditions.add("(title LIKE ? OR description LIKE ?)")
            args.add("%$query%")
            args.add("%$query%")
        }
        if (!status.isNullOrEmpty()) {
            conditions.add("status = ?")
            args.add(status)
        }
        if (!category.isNullOrEmpty()) {
            conditions.add("category = ?")
            args.add(category)
        }

        val selection = conditions.joinToString(" AND ")
        val cursor = db.query("tasks", null, selection, args.toTypedArray(), null, null, "id DESC")

        while (cursor.moveToNext()) {
            list.add(
                Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    priority = cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                )
            )
        }
        cursor.close()
        return list
    }

    // Aqui se consulta el Historial
    fun getCompletedTasks(userId: Int): List<Task> {
        val list = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM tasks WHERE userId = ? AND status = 'COMPLETADA' ORDER BY id DESC",
            arrayOf(userId.toString())
        )

        while (cursor.moveToNext()) {
            list.add(
                Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    priority = cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                )
            )
        }
        cursor.close()
        return list
    }
}