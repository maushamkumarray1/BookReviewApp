package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // 🔹 Login Button Click Listener with Coroutine
        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                loginUser()
            }
        }

        // 🔹 Forgot Password Click Listener
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // 🔹 Register Click Listener
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private suspend fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }

        // ✅ Show progress bar before login starts
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null && user.isEmailVerified) {
                showToast("Login successful!")
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                showToast("Please verify your email before logging in.")
                auth.signOut()
            }
        } catch (e: Exception) {
            showToast(e.message ?: "Login failed")
        } finally {
            // ✅ Hide progress bar after login completes
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
        }
    }


    // Function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
