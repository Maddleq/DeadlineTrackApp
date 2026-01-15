package com.example.deadlinetrackapp

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PinStore {

    private const val PREFS = "pin_prefs"
    private const val KEY_HAS_PIN = "has_pin"
    private const val KEY_PIN = "pin" // заглушка: храним как строку

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun hasPin(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAS_PIN, false)

    fun savePin(context: Context, pin: String) {
        prefs(context).edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_HAS_PIN, true)
            .apply()
    }

    fun checkPin(context: Context, pin: String): Boolean =
        prefs(context).getString(KEY_PIN, null) == pin

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
