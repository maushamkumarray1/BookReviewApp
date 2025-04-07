package com.example.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookAdapter(
    private var bookList: MutableList<Book>,
    private val onEditClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val firestore = FirebaseFirestore.getInstance()

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val author: TextView = itemView.findViewById(R.id.tvAuthor)
        val genre: TextView = itemView.findViewById(R.id.tvGenre)
        val latestReview: TextView = itemView.findViewById(R.id.tvLatestReview) // Displays latest review
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar) // ✅ Displays rating as stars
        val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btnMoreOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.genre.text = book.genre
        holder.latestReview.text = book.latestReview.ifEmpty { "No reviews yet" } // Show latest review

        // ✅ Fetch and Display the Latest Rating in Real-Time
        loadBookRating(book.id, holder.ratingBar)

        holder.btnMoreOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.btnMoreOptions)
            popupMenu.inflate(R.menu.book_options_menu)

            // Check if the current user is the book admin
            val isBookAdmin = book.adminId == currentUserId

            // Hide edit & delete options if the user is not the book admin
            popupMenu.menu.findItem(R.id.action_edit).isVisible = isBookAdmin
            popupMenu.menu.findItem(R.id.action_delete).isVisible = isBookAdmin

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        if (isBookAdmin) {
                            onEditClick(book)
                        } else {
                            showUnauthorizedMessage(holder)
                        }
                        true
                    }
                    R.id.action_delete -> {
                        if (isBookAdmin) {
                            onDeleteClick(book)
                        } else {
                            showUnauthorizedMessage(holder)
                        }
                        true
                    }
                    R.id.action_review -> {
                        // ✅ Open ReviewActivity with BOOK_ID
                        val intent = Intent(holder.itemView.context, ReviewActivity::class.java)
                        intent.putExtra("BOOK_ID", book.id)
                        holder.itemView.context.startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = bookList.size

    fun updateBooks(newBooks: MutableList<Book>) {
        bookList = newBooks
        notifyDataSetChanged()
    }

    // ✅ Fetch the latest book rating in real-time
    private fun loadBookRating(bookId: String, ratingBar: RatingBar) {
        firestore.collection("books").document(bookId)
            .collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val ratings = snapshot.documents.mapNotNull { it.getDouble("rating") }
                val averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f

                ratingBar.rating = averageRating // Set Rating Bar Value
            }
    }

    private fun showUnauthorizedMessage(holder: BookViewHolder) {
        android.widget.Toast.makeText(
            holder.itemView.context,
            "Only the book admin can perform this action.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
