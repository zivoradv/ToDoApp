package com.example.to_doapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth // Add this line
    private val PREFS_NAME = "MyPrefs"
    private val PREF_LOGGED_IN = "isLoggedIn"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        displayUserDetails()

        setupButtonListeners()
    }

    private fun displayUserDetails() {
        val currentUser = auth.currentUser

        currentUser?.let {
            val username = currentUser.displayName // Username
            val email = currentUser.email // Email

            Log.d("YourTag", "User signed in. UID: ${currentUser.displayName}, Email: ${currentUser.email}")

            // Assuming your layout has TextViews to display this information
            val textViewUsername = findViewById<TextView>(R.id.textViewUsername)
            val textViewEmail = findViewById<TextView>(R.id.textViewEmail)

            textViewUsername.text = "Username: zivorad"
            textViewEmail.text = "Email: $email"
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putBoolean(PREF_LOGGED_IN, false)
        editor.apply()

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupButtonListeners() {
        val changePasswordButton = findViewById<Button>(R.id.buttonChangePassword)
        val logoutButton = findViewById<Button>(R.id.buttonLogout)

        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val newPassword = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPassword = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Change") { dialog, _ ->
                val newPasswordText = newPassword.text.toString()
                val confirmPasswordText = confirmPassword.text.toString()

                if (newPasswordText == confirmPasswordText) {
                    updatePassword(newPasswordText)
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Password's do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun updatePassword(newPassword: String) {
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Password updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Password update failed: ${task.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

}
