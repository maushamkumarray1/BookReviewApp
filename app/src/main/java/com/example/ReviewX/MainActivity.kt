package com.example.myapplication // Replace with your package name

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // ✅ Check if the correct theme is applied
        setTheme(R.style.Theme_MyApplication)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // 🔹 Check for network connection before proceeding
        if (!isNetworkAvailable()) {
            showToast("No internet connection. Please check your network.")
        } else {
            checkUserLoginStatus()
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * 🔹 Check if user is already logged in.
     */
    private fun checkUserLoginStatus() {
        if (auth.currentUser != null) {
            Log.d("MainActivity", "User already logged in: ${auth.currentUser?.email}")
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            Log.d("MainActivity", "No user logged in.")
        }
    }

    /**
     * 🔹 Check if device has an active internet connection.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    /**
     * 🔹 Display a toast message.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
