package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.databinding.ItemBookAllbooksBinding
import com.okta.senov.fragment.HomeFragmentDirections
import com.okta.senov.model.Book

class AllBooksAdapter(
    private val books: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<AllBooksAdapter.AllBooksViewHolder>() {

    inner class AllBooksViewHolder(
        private val binding: ItemBookAllbooksBinding,
        private val onItemClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title
//                bookAuthorTextView.text = book.author
//                bookPriceTextView.text = book.price.toString()
                Glide.with(binding.root.context)
                    .load(book.coverResourceId)
                    .into(bookCoverImageView)
//                bookRatingBar.rating = book.rating

                root.setOnClickListener { onItemClick(book) }

                buttonGoToDetail.setOnClickListener {
                    val navController = binding.root.findNavController()
                    val action = HomeFragmentDirections.actionHomeToDetail(book)
                    navController.navigate(action)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllBooksViewHolder {
        val binding = ItemBookAllbooksBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AllBooksViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AllBooksViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size
}