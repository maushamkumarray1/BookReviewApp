package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityDashboarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboarBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var bookAdapter: BookAdapter
    private var bookList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Set Toolbar as ActionBar
        setSupportActionBar(binding.toolbar)

        // Setup RecyclerView
        bookAdapter = BookAdapter(
            bookList,
            onEditClick = { book -> editBook(book) },
            onDeleteClick = { book -> deleteBook(book) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = bookAdapter

        binding.btnAddBook.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }

        loadBooks()
    }

    // Load books from Firestore for the logged-in user
    private fun loadBooks() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("books")
            .get()
            .addOnSuccessListener { documents ->
                bookList.clear()
                for (document in documents) {
                    val book = document.toObject(Book::class.java).apply {
                        id = document.id // Ensure the book has its Firestore ID
                    }
                    bookList.add(book)
                }
                bookAdapter.updateBooks(bookList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load books!", Toast.LENGTH_SHORT).show()
            }
    }

    // Edit a book
    private fun editBook(book: Book) {
        val intent = Intent(this, EditBookActivity::class.java)
        intent.putExtra("bookId", book.id)
        intent.putExtra("title", book.title)
        intent.putExtra("author", book.author)
        intent.putExtra("genre", book.genre)
        startActivity(intent)
    }

    // Delete a book
    private fun deleteBook(book: Book) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("books").document(book.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Book deleted!", Toast.LENGTH_SHORT).show()
                loadBooks()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete book!", Toast.LENGTH_SHORT).show()
            }
    }

    // Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    // Handle menu item click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Logout function
    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Close DashboardActivity
    }
}
