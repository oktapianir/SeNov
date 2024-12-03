package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.databinding.ItemBookAllbooksBinding
import com.okta.senov.databinding.ItemBookBinding
import com.okta.senov.fragment.HomeFragmentDirections
import com.okta.senov.model.Book

class BookAdapter(
    private val books: List<Book>,
    private val isPopular: Boolean // Flag untuk menentukan layout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_POPULAR = 1
        private const val VIEW_TYPE_ALL_BOOKS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPopular) VIEW_TYPE_POPULAR else VIEW_TYPE_ALL_BOOKS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POPULAR -> {
                val binding = ItemBookBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PopularViewHolder(binding)
            }
            VIEW_TYPE_ALL_BOOKS -> {
                val binding = ItemBookAllbooksBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AllBooksViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val book = books[position]
        when (holder) {
            is PopularViewHolder -> holder.bind(book)
            is AllBooksViewHolder -> holder.bind(book)
        }
    }

    override fun getItemCount(): Int = books.size

    // ViewHolder untuk Popular Books
    inner class PopularViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title
                bookAuthorTextView.text = book.author
                bookPriceTextView.text = book.price
                bookRatingBar.rating = book.rating
                bookCoverImageView.setImageResource(book.coverResourceId)

                buttonGoToDetail.setOnClickListener {
                    val navController = binding.root.findNavController()
                    val action = HomeFragmentDirections.actionHomeToDetail(book)
                    navController.navigate(action)
                }
            }
        }
    }

    // ViewHolder untuk All Books
    inner class AllBooksViewHolder(private val binding: ItemBookAllbooksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title
                bookAuthorTextView.text = book.author
                bookCategoryTextView.text = book.genre
                bookPriceTextView.text = book.price
                bookRatingBar.rating = book.rating
                bookCoverImageView.setImageResource(book.coverResourceId)

                buttonGoToDetail.setOnClickListener {
                    val navController = binding.root.findNavController()
                    val action = HomeFragmentDirections.actionHomeToDetail(book)
                    navController.navigate(action)
                }
            }
        }
    }
}
