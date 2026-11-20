package com.example.todoapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferencesManager = PreferencesManager(this)

        val rgTheme = findViewById<RadioGroup>(R.id.rgTheme)
        val rbLight = findViewById<RadioButton>(R.id.rbLight)
        val rbDark = findViewById<RadioButton>(R.id.rbDark)
        val switchNotifications = findViewById<SwitchCompat>(R.id.switchNotifications)
        val rgLanguage = findViewById<RadioGroup>(R.id.rgLanguage)
        val rbSpanish = findViewById<RadioButton>(R.id.rbSpanish)
        val rbEnglish = findViewById<RadioButton>(R.id.rbEnglish)
        val btnReset = findViewById<Button>(R.id.btnResetSettings)

        // 1. Cargar valores guardados en DataStore al abrir la pantalla
        lifecycleScope.launch {
            val isDark = preferencesManager.isDarkThemeFlow.first()
            val notifications = preferencesManager.notificationsEnabledFlow.first()
            val lang = preferencesManager.languageFlow.first()

            rbDark.isChecked = isDark
            rbLight.isChecked = !isDark
            switchNotifications.isChecked = notifications
            rbEnglish.isChecked = (lang == "en")
            rbSpanish.isChecked = (lang != "en")

            // 2. Activar listeners para guardar automáticamente cada cambio
            rgTheme.setOnCheckedChangeListener { _, checkedId ->
                val darkSelected = (checkedId == R.id.rbDark)
                lifecycleScope.launch {
                    preferencesManager.saveTheme(darkSelected)
                    AppCompatDelegate.setDefaultNightMode(
                        if (darkSelected) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
            }

            switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    preferencesManager.saveNotifications(isChecked)
                }
            }

            rgLanguage.setOnCheckedChangeListener { _, checkedId ->
                val selectedLang = if (checkedId == R.id.rbEnglish) "en" else "es"
                lifecycleScope.launch {
                    preferencesManager.saveLanguage(selectedLang)
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(selectedLang)
                    )
                }
            }
        }

        // 3. Restablecer configuración predeterminada
        btnReset.setOnClickListener {
            lifecycleScope.launch {
                preferencesManager.resetPreferences()
                rbLight.isChecked = true
                switchNotifications.isChecked = true
                rbSpanish.isChecked = true

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags("es")
                )
                Toast.makeText(this@SettingsActivity, "Configuración restablecida", Toast.LENGTH_SHORT).show()
            }
        }
    }
}