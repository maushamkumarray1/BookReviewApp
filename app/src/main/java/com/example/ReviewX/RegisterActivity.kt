package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Set up register button click listener
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!isValidEmail(email)) {
                binding.etEmail.error = "Enter a valid email"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                binding.passwordLayout.error = "Password must contain:\n• 8+ characters\n• 1 uppercase\n• 1 lowercase\n• 1 digit\n• 1 special character"
                return@setOnClickListener
            } else {
                binding.passwordLayout.error = null  // Clear error if valid
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        showToast("Registration successful! Please verify your email.")

                        user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                showToast("Verification email sent. Please verify before login.")
                                auth.signOut() // Logout user so they verify first
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            } else {
                                showToast("Failed to send verification email.")
                            }
                        }
                    } else {
                        task.exception?.message?.let {
                            showToast(it)  // Display Firebase error message
                        }
                    }
                }
        }
    }

    // Function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate strong password
    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!.,?]).{8,}\$"
        return password.matches(passwordPattern.toRegex())
    }
}
