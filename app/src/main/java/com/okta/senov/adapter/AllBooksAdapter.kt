package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.databinding.ItemBookAllbooksBinding
import com.okta.senov.model.Book

class AllBooksAdapter(
    private val books: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<AllBooksAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookAllbooksBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount() = books.size

    class BookViewHolder(private val binding: ItemBookAllbooksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title
                bookAuthorTextView.text = book.author
                bookCategoryTextView.text = book.genre
                bookPriceTextView.text = book.price
                bookRatingBar.rating = book.rating

                // Tambahkan logging atau debug
                try {
                    bookCoverImageView.setImageResource(book.coverResourceId)
                } catch (e: Exception) {
                    // Log error jika gambar tidak bisa di-set
                    e.printStackTrace()
                    // Bisa set placeholder image jika diperlukan
                    // bookCoverImageView.setImageResource(R.drawable.default_book_cover)
                }
            }
        }
    }
}