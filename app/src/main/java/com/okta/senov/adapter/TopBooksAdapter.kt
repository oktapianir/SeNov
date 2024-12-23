package com.okta.senov.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookBinding
import com.okta.senov.model.Book

class TopBooksAdapter(
    private var books: List<Book>
) : RecyclerView.Adapter<TopBooksAdapter.BookViewHolder>() {

    class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title

                bookCoverImageView.load(book.coverResourceId) {
                    crossfade(true)
                    placeholder(R.drawable.img_book_cover1)
                    error(R.drawable.img_book_cover1)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
