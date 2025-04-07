package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityDashboarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboarBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var bookAdapter: BookAdapter
    private var bookList = mutableListOf<Book>()
    private var filteredList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)

        // ✅ Book Adapter with UI Enhancements
        bookAdapter = BookAdapter(
            filteredList,
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

    private fun loadBooks() {
        firestore.collection("books")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading books!", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                bookList.clear()
                snapshots?.documents?.forEach { document ->
                    val book = document.toObject(Book::class.java)?.apply { id = document.id }
                    if (book != null) bookList.add(book)
                }

                loadReviewsAndRatings() // ✅ Calls both reviews & ratings functions
            }
    }

    // ✅ New function to load both latest reviews and ratings
    private fun loadReviewsAndRatings() {
        loadLatestReviews() // Fetch latest review for each book
        bookList.forEach { fetchBookRating(it) } // Fetch ratings for each book
    }

    private fun loadLatestReviews() {
        bookList.forEach { book ->
            val bookRef = firestore.collection("books").document(book.id)

            // Fetch latest review text
            bookRef.collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { reviewSnapshot, error ->
                    if (error == null) {
                        val latestReview = reviewSnapshot?.documents?.firstOrNull()?.getString("reviewText")
                        book.latestReview = latestReview ?: "No reviews yet"
                        updateBookList()
                    }
                }

            // Fetch average rating
            bookRef.collection("reviews")
                .addSnapshotListener { ratingSnapshot, error ->
                    if (error == null) {
                        val ratings = ratingSnapshot?.documents?.mapNotNull { it.getDouble("rating") } ?: emptyList()
                        book.averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f
                        updateBookList() // Update UI after fetching ratings
                    }
                }
        }
    }

    private fun fetchBookRating(book: Book) {
        firestore.collection("books").document(book.id)
            .collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val ratings = snapshot.documents.mapNotNull { it.getDouble("rating") }
                val averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f

                book.averageRating = averageRating
                updateBookList() // ✅ Update UI after getting reviews & ratings
            }
    }

    private fun updateBookList() {
        filteredList.clear()
        filteredList.addAll(bookList)
        bookAdapter.updateBooks(filteredList)
    }

    private fun editBook(book: Book) {
        val userId = auth.currentUser?.uid
        if (book.adminId == userId) {
            val intent = Intent(this, EditBookActivity::class.java)
            intent.putExtra("bookId", book.id)
            intent.putExtra("title", book.title)
            intent.putExtra("author", book.author)
            intent.putExtra("genre", book.genre)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Only the book owner can edit!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteBook(book: Book) {
        val userId = auth.currentUser?.uid
        if (book.adminId == userId) {
            firestore.collection("books").document(book.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Book deleted!", Toast.LENGTH_SHORT).show()
                    loadBooks()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete book!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Only the book owner can delete!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Search books..."
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBooks(newText.orEmpty())
                return true
            }
        })
        return true
    }

    private fun filterBooks(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(bookList)
        } else {
            filteredList.addAll(bookList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true) ||
                        it.genre.contains(query, ignoreCase = true)
            })
        }
        bookAdapter.updateBooks(filteredList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
