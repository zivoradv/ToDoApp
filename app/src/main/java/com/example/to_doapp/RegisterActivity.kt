package com.example.to_doapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.example.to_doapp.databinding.ActivityRegisterBinding
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegisterUser.setOnClickListener {
            registerUser()
        }

        val textLoginNow = findViewById<TextView>(R.id.textLoginNow)

        textLoginNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser() {
        val email = binding.editTextEmailRegister.text.toString()
        val username = binding.editTextUsername.text.toString()
        val password = binding.editTextPasswordRegister.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userId = user?.uid

                    userId?.let {
                        val userRef =
                            FirebaseDatabase.getInstance("https://to-do-80f5e-default-rtdb.europe-west1.firebasedatabase.app").reference.child(
                                "users"
                            ).child(it)
                        val userData = HashMap<String, Any>()
                        userData["username"] = username
                        userRef.setValue(userData)
                            .addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    startActivity(Intent(this, TaskActivity::class.java))
                                } else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Error: ${databaseTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}

