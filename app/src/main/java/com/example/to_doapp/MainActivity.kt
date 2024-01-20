package com.example.to_doapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "MyPrefs"
    private val PREF_LOGGED_IN = "isLoggedIn"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(PREF_LOGGED_IN, false)

        if (isLoggedIn) {
            startActivity(Intent(this, TaskActivity::class.java))
            finish()
        }
    }

    fun onLoginButtonClick(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun onRegisterButtonClick(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}



