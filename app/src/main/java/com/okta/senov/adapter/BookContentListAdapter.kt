package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookContentListBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent

class BookContentListAdapter(
    private val onBookClick: (Book, BookContent) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : ListAdapter<Pair<Book, BookContent>, BookContentListAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookContentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(private val binding: ItemBookContentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bookPair: Pair<Book, BookContent>) {
            val (book, bookContent) = bookPair

            binding.apply {
                bookNameTextView.text = book.title
                // Display chapter count information
                chaptersTextView.text = "${bookContent.chapters.size} Chapters"

                // Load book cover image with Glide
                Glide.with(bookImageView.context)
                    .load(book.image)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(bookImageView)

                // Item click listener - will navigate to chapters
                root.setOnClickListener {
                    onBookClick(book, bookContent)
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(book)
                }
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Pair<Book, BookContent>>() {
        override fun areItemsTheSame(oldItem: Pair<Book, BookContent>, newItem: Pair<Book, BookContent>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Pair<Book, BookContent>, newItem: Pair<Book, BookContent>): Boolean {
            return oldItem == newItem
        }
    }
}