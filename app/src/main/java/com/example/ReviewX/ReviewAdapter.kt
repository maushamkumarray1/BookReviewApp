package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView





class ReviewAdapter(
    private var reviewList: MutableList<Review>,
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewText: TextView = itemView.findViewById(R.id.tvReviewText)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarReview)

        val btnEdit: Button = itemView.findViewById(R.id.btnEditReview)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.tvReviewText.text = review.reviewText
        holder.ratingBar.rating = review.rating

        holder.btnEdit.setOnClickListener { onEditClick(review) }
        holder.btnDelete.setOnClickListener { onDeleteClick(review) }
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateList(newList: MutableList<Review>) {
        reviewList = newList
        notifyDataSetChanged()
    }
}
