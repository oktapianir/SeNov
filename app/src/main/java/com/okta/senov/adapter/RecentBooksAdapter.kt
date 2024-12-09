package com.okta.senov.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.databinding.RecentBooksBinding
import com.okta.senov.model.Book

class RecentBooksAdapter(private val books: List<Book>) : RecyclerView.Adapter<RecentBooksAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = RecentBooksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(private val binding: RecentBooksBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.bookCover.setImageResource(book.coverResourceId)
            binding.bookTitle.text = book.title
            binding.bookGenre.text = book.genre
            binding.bookRating.text = "Rating: ${book.rating}"
            binding.bookPrice.text = book.price
        }
    }
}
