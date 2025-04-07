package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

        // ✅ Initialize Firebase
        FirebaseApp.initializeApp(this)

        // ✅ Apply theme before setting content view
        setTheme(R.style.Theme_MyApplication)

        // ✅ Setup View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // ✅ Check for internet connection before proceeding
        if (!isNetworkAvailable()) {
            showToast("No internet connection. Please check your network.")
            return
        }

        // ✅ Check if user is logged in
        checkUserLoginStatus()

        // ✅ Setup button listeners
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * ✅ Check if user is already logged in and redirect to Dashboard.
     */
    private fun checkUserLoginStatus() {
        auth.currentUser?.let {
            Log.d("MainActivity", "User already logged in: ${it.email}")
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()  // Close MainActivity
        }
    }

    /**
     * ✅ Logout user and clear session.
     */

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logout successful!", Toast.LENGTH_SHORT).show()

        // Clear session data (Optional)
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Redirect to login screen
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * ✅ Check if device has an active internet connection.
     */

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * ✅ Display a toast message.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
