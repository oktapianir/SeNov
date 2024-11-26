package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.model.Book
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookBinding // Import the generated binding class

class BookAdapter(
    private val books: List<Book>
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title
                bookAuthorTextView.text = book.author
                bookCategoryTextView.text = book.genre
                bookPriceTextView.text = book.price
                bookRatingBar.rating = book.rating
                bookCoverImageView.setImageResource(book.coverResourceId)

                // Set click listener for the Go to Detail button
                buttonGoToDetail.setOnClickListener {
                    // Get NavController from any view in the RecyclerView
                    val navController = binding.root.findNavController()
                    navController.navigate(R.id.action_home_to_detail)
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
}
