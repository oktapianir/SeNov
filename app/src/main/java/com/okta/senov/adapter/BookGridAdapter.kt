package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.databinding.ItemBookGridBinding
import com.okta.senov.model.Book

class BookGridAdapter(
    private var books: List<Book>,
    private val onBookClicked: (Book) -> Unit
) : RecyclerView.Adapter<BookGridAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    inner class BookViewHolder(private val binding: ItemBookGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookClicked(books[position])
                }
            }
        }

        fun bind(book: Book) {
            binding.bookTitleText.text = book.title
            binding.bookAuthorText.text = book.authorName

            // Load book cover image using Glide
            Glide.with(binding.bookCoverImage.context)
                .load(book.image)
                .centerCrop()
                .into(binding.bookCoverImage)
        }
    }
}