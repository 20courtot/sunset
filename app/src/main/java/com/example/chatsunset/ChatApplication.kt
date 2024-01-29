package com.example.chatsunset

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}