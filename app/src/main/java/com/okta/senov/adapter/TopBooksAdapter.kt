package com.okta.senov.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.R
import com.okta.senov.databinding.RecentBooksBinding
import com.okta.senov.databinding.TopBooksBinding
import com.okta.senov.model.Book

class TopBooksAdapter(
    private var books: List<Book>,
    private val isRecent: Boolean = false
) : RecyclerView.Adapter<TopBooksAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (isRecent) {
            BookViewHolder.RecentBooksHolder(
                RecentBooksBinding.inflate(layoutInflater, parent, false)
            )
        } else {
            BookViewHolder.TopBooksHolder(
                TopBooksBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        when (holder) {
            is BookViewHolder.TopBooksHolder -> holder.bind(book)
            is BookViewHolder.RecentBooksHolder -> holder.bind(book)
        }
    }

    override fun getItemCount(): Int = books.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    sealed class BookViewHolder(binding: androidx.viewbinding.ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class TopBooksHolder(private val binding: TopBooksBinding) : BookViewHolder(binding) {
            fun bind(book: Book) {
                binding.apply {
                    bookTitle.text = book.title
                    bookCover.setImageResource(R.drawable.img_book_cover2)
                }
            }
        }

        class RecentBooksHolder(private val binding: RecentBooksBinding) : BookViewHolder(binding) {
            fun bind(book: Book) {
                binding.apply {
                    bookTitle.text = book.title
                    bookCover.setImageResource(R.drawable.img_book_cover1)
                }
            }
        }
    }
}
