package com.example.todoapp.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val ACTIVE_USER_ID = intPreferencesKey("active_user_id")

        // Llaves dinámicas por cada usuario
        private fun darkThemeKey(userId: Int) = booleanPreferencesKey("user_${userId}_is_dark_theme")
        private fun notificationsKey(userId: Int) = booleanPreferencesKey("user_${userId}_notifications_enabled")
        private fun languageKey(userId: Int) = stringPreferencesKey("user_${userId}_language_code")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    val activeUserIdFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_USER_ID] ?: -1
    }

    // Si no hay sesión (-1), devuelve valores predeterminados; si hay usuario, lee los suyos
    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val userId = prefs[ACTIVE_USER_ID] ?: -1
        if (userId == -1) false else (prefs[darkThemeKey(userId)] ?: false)
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val userId = prefs[ACTIVE_USER_ID] ?: -1
        if (userId == -1) true else (prefs[notificationsKey(userId)] ?: true)
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val userId = prefs[ACTIVE_USER_ID] ?: -1
        if (userId == -1) "es" else (prefs[languageKey(userId)] ?: "es")
    }

    suspend fun saveSession(isLoggedIn: Boolean, userId: Int = -1) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = isLoggedIn
            prefs[ACTIVE_USER_ID] = userId
        }
    }

    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            val userId = prefs[ACTIVE_USER_ID] ?: -1
            if (userId != -1) {
                prefs[darkThemeKey(userId)] = isDark
            }
        }
    }

    suspend fun saveNotifications(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val userId = prefs[ACTIVE_USER_ID] ?: -1
            if (userId != -1) {
                prefs[notificationsKey(userId)] = enabled
            }
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            val userId = prefs[ACTIVE_USER_ID] ?: -1
            if (userId != -1) {
                prefs[languageKey(userId)] = languageCode
            }
        }
    }

    suspend fun resetPreferences() {
        context.dataStore.edit { prefs ->
            val userId = prefs[ACTIVE_USER_ID] ?: -1
            if (userId != -1) {
                prefs[darkThemeKey(userId)] = false
                prefs[notificationsKey(userId)] = true
                prefs[languageKey(userId)] = "es"
            }
        }
    }
}