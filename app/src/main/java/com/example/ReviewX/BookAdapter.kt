package com.example.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private var bookList: MutableList<Book>,
    private val onEditClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val author: TextView = itemView.findViewById(R.id.tvAuthor)
        val genre: TextView = itemView.findViewById(R.id.tvGenre)
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

        // Show Popup Menu when clicking on More Options (three-dot button)
        holder.btnMoreOptions.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.btnMoreOptions)
            popupMenu.inflate(R.menu.book_options_menu)

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(book) // Call the edit function
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(book) // Call the delete function
                        true
                    }
                    R.id.action_review -> {
                        val intent = Intent(holder.itemView.context, ReviewActivity::class.java)
                        intent.putExtra("bookId", book.id)
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
}
