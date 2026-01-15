package com.example.deadlinetrackapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.deadlinetrackapp.databinding.ActivityPinBinding

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private var isSetupMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSetupMode = !PinStore.hasPin(this)
        renderMode()

        binding.btnAction.setOnClickListener {
            binding.tvError.visibility = View.GONE
            if (isSetupMode) handleSetup() else handleUnlock()
        }
    }

    private fun renderMode() {
        if (isSetupMode) {
            binding.tvTitle.text = "Создайте PIN"
            binding.etPinConfirm.visibility = View.VISIBLE
            binding.btnAction.text = "Сохранить PIN"
        } else {
            binding.tvTitle.text = "Введите PIN"
            binding.etPinConfirm.visibility = View.GONE
            binding.btnAction.text = "Войти"
        }
    }

    private fun handleSetup() {
        val pin = binding.etPin.text.toString()
        val confirm = binding.etPinConfirm.text.toString()

        when {
            pin.length != 4 -> showError("PIN должен состоять из 4 цифр")
            pin != confirm -> showError("PIN и подтверждение не совпадают")
            else -> {
                PinStore.savePin(this, pin)
                goToMain()
            }
        }
    }

    private fun handleUnlock() {
        val pin = binding.etPin.text.toString()

        when {
            pin.length != 4 -> showError("PIN должен состоять из 4 цифр")
            !PinStore.checkPin(this, pin) -> showError("Неверный PIN")
            else -> goToMain()
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
