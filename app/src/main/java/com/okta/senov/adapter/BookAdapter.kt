package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookBinding
import com.okta.senov.model.BookData
import timber.log.Timber

class BookAdapter(
    private val onItemClick: (BookData) -> Unit,
    private val onRemoveClick: (BookData) -> Unit
) : ListAdapter<BookData, BookAdapter.BookViewHolder>(DIFF_CALLBACK) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookData) {
            // Set book title
            binding.bookTitleTextView.text = book.title

            // Load book image
            Glide.with(binding.root.context)
                .load(book.image)
                .placeholder(R.drawable.img_book_cover1)
                .error(R.drawable.img_book_cover1)
                .into(binding.bookCoverImageView)

            // Set up remove button
            binding.btnRemove.setOnClickListener {
                onRemoveClick(book)
            }

            // Set up item click listener
            binding.root.setOnClickListener {
                onItemClick(book)
            }

            // Initially set author name from the book data
            binding.bookAuthorTextView.text = book.authorName

            // Fetch and set reading progress
            fetchReadingProgress(book.id)
        }

        private fun fetchReadingProgress(bookId: String) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Fetch reading progress
                db.collection("users").document(userId)
                    .collection("reading_progress").document(bookId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val readingPercentage = document.getDouble("percentage") ?: 0.0
                            binding.readPercentageTextView.text = String.format("%.1f%%", readingPercentage)
                            Timber.tag("ReadingProgress").d("Progress for book $bookId: $readingPercentage%")
                        } else {
                            // No reading progress found, set to 0%
                            binding.readPercentageTextView.text = "0.0%"
                            Timber.tag("ReadingProgress").d("No progress found for book $bookId")
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.readPercentageTextView.text = "0.0%"
                        Timber.tag("ReadingProgressError").e("Error fetching reading progress: ${e.message}")
                    }
            } else {
                binding.readPercentageTextView.text = "0.0%"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookData>() {
            override fun areItemsTheSame(oldItem: BookData, newItem: BookData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: BookData, newItem: BookData): Boolean {
                return oldItem == newItem
            }
        }
    }
}