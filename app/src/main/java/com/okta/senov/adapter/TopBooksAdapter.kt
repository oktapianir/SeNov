package com.okta.senov.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.okta.senov.R
import com.okta.senov.databinding.RecentBooksBinding
import com.okta.senov.databinding.TopBooksBinding
import com.okta.senov.model.Book

class TopBooksAdapter(
    private var books: List<Book>,
    private val viewType: ViewType = ViewType.TOP
) : RecyclerView.Adapter<TopBooksAdapter.BookViewHolder>() {

    enum class ViewType {
        TOP, RECENT, SEARCH_RESULT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (this.viewType) {
            ViewType.RECENT -> BookViewHolder.RecentBooksHolder(
                RecentBooksBinding.inflate(layoutInflater, parent, false)
            )

            ViewType.TOP -> BookViewHolder.TopBooksHolder(
                TopBooksBinding.inflate(layoutInflater, parent, false)
            )

            ViewType.SEARCH_RESULT -> BookViewHolder.SearchResultHolder(
                RecentBooksBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        when (holder) {
            is BookViewHolder.TopBooksHolder -> holder.bind(book)
            is BookViewHolder.RecentBooksHolder -> holder.bind(book)
            is BookViewHolder.SearchResultHolder -> holder.bind(book)
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
                    bookCover.setImageResource(R.drawable.img_book_content)
                }
            }
        }

        class RecentBooksHolder(private val binding: RecentBooksBinding) : BookViewHolder(binding) {
            fun bind(book: Book) {
                binding.apply {
                    bookTitle.text = book.title
                    bookAuthor.text = book.authorName
                    // Load the image from URL using Glide
                    if (!book.image.isNullOrEmpty()) {
                        Glide.with(binding.root.context)
                            .load(book.image)
                            .placeholder(R.drawable.img_book_placeholder)
                            .error(R.drawable.ic_error)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(binding.bookCover)
                    } else {
                        // Use placeholder if no image URL is available
                        binding.bookCover.setImageResource(R.drawable.img_book_placeholder)
                    }                }
            }
        }

        class SearchResultHolder(private val binding: RecentBooksBinding) :
            BookViewHolder(binding) {
            fun bind(book: Book) {
                binding.apply {
                    bookTitle.text = book.title
                    bookAuthor.text = book.authorName
                    bookGenre.text = book.category ?: "Unknown"
                    // Load the image from URL using Glide
                    if (!book.image.isNullOrEmpty()) {
                        Glide.with(binding.root.context)
                            .load(book.image)
                            .placeholder(R.drawable.img_book_placeholder)
                            .error(R.drawable.ic_error)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(binding.bookCover)
                    } else {
                        // Use placeholder if no image URL is available
                        binding.bookCover.setImageResource(R.drawable.img_book_placeholder)
                    }
                }
            }
        }
    }
}

