package com.example.new1

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SerialNumberManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SerialNumberPrefs", Context.MODE_PRIVATE)
    private val defaultSerialNumber = "0000000000"
    
    var serialNumber by mutableStateOf(
        prefs.getString("serial_number", defaultSerialNumber) ?: defaultSerialNumber
    )
        private set

    fun updateSerialNumber(newSerialNumber: String) {
        if (newSerialNumber.length == 10) {
            serialNumber = newSerialNumber
            prefs.edit().putString("serial_number", newSerialNumber).apply()
        }
    }
} 