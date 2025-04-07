package com.example.myapplication

import android.view.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ReviewAdapter(
    private var reviewList: MutableList<Review>,
    private val currentUserId: String,
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewText: TextView = itemView.findViewById(R.id.tvReviewText)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarReview)
        val menuButton: ImageView = itemView.findViewById(R.id.ivMenu)  // Three-dot menu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.tvReviewText.text = review.reviewText
        holder.ratingBar.rating = review.rating

        // Show menu only if the user is the owner of the review
        if (currentUserId == review.userId) {
            holder.menuButton.visibility = View.VISIBLE
            holder.menuButton.setOnClickListener { showPopupMenu(it, review) }
        } else {
            holder.menuButton.visibility = View.GONE
        }
    }

    private fun showPopupMenu(view: View, review: Review) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.review_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    onEditClick(review)
                    true
                }
                R.id.menu_delete -> {
                    onDeleteClick(review)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateList(newList: MutableList<Review>) {
        reviewList.clear()
        reviewList.addAll(newList)
        notifyDataSetChanged()
    }
}
