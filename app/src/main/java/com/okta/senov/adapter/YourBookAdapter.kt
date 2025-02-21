package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.databinding.ItemYourBookBinding
import com.okta.senov.model.Book

class YourBookAdapter(
    private val onItemClick: (Book) -> Unit,
    private val onProgressUpdate: (Book, Int) -> Unit
) : ListAdapter<Book, YourBookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemYourBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(private val binding: ItemYourBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                // Set book details
                tvBookTitle.text = book.title
//                tvAuthor.text = book.author
//                tvProgress.text = "Progress: ${book.readingProgress}%"
//                ratingBar.rating = book.rating

                // Load book cover
                Glide.with(ivBookCover.context)
                    .load(book.coverResourceId)
                    .into(ivBookCover)

                // Set click listener
                root.setOnClickListener { onItemClick(book) }
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}