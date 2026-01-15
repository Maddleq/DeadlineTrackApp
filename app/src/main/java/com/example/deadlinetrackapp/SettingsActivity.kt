package com.example.deadlinetrackapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.deadlinetrackapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val prefs by lazy {
        getSharedPreferences("settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requirePin = prefs.getBoolean("require_pin", false)
        binding.swRequirePin.isChecked = requirePin

        binding.swRequirePin.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("require_pin", isChecked).apply()
        }

        // Toolbar + "Назад"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // --- ТЕМА (Day/Night) ---
        val isDark = prefs.getBoolean("dark_theme", false)
        binding.swDarkTheme.isChecked = isDark

        binding.swDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_theme", isChecked).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate() // чтобы сразу увидеть смену темы
        }

        // --- ЯЗЫК (RU/EN) ---
        binding.btnLangRu.setOnClickListener {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags("ru")
            )
        }

        binding.btnLangEn.setOnClickListener {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags("en")
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
