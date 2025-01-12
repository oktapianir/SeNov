package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookBinding
import com.okta.senov.model.BookData

class BookAdapter(private val books: List<BookData>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookData) {
            binding.bookTitleTextView.text = book.title
            Glide.with(binding.root.context)
                .load(book.image)
                .placeholder(R.drawable.img_book_cover1)
                .into(binding.bookCoverImageView)
        }
    }
}
