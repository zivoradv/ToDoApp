package com.example.to_doapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.to_doapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val PREFS_NAME = "MyPrefs"
    private val PREF_LOGGED_IN = "isLoggedIn"

    private fun checkLoggedInStatus() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(PREF_LOGGED_IN, false)

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setLoggedInStatus() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_LOGGED_IN, true)
        editor.apply()
    }

    private fun clearLoggedInStatus() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_LOGGED_IN, false)
        editor.apply()
    }

    private fun loginUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this@LoginActivity,
                "Please enter both email and password.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setLoggedInStatus()

                    startActivity(Intent(this, TaskActivity::class.java))
                    finish()
                } else {
                    if (task.exception?.message == "The password is invalid or the user does not have a password.") {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid credentials. Please check your email and password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLoggedInStatus()

        binding.btnLoginUser.setOnClickListener {
            loginUser()
        }

        val textLoginNow = findViewById<TextView>(R.id.textRegisterNow)

        textLoginNow.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }


}
