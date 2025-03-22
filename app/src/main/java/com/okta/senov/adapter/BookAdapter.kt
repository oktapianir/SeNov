package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookBinding
import com.okta.senov.model.BookData
import timber.log.Timber

class BookAdapter(
    private val onItemClick: (BookData) -> Unit,
    private val onRemoveClick: (BookData) -> Unit
) : ListAdapter<BookData, BookAdapter.BookViewHolder>(DIFF_CALLBACK) {

    private val db = FirebaseFirestore.getInstance()

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookData) {
            binding.bookTitleTextView.text = book.title
            binding.bookAuthorTextView.text = "Loading..."
            Glide.with(binding.root.context)
                .load(book.image)
                .placeholder(R.drawable.img_book_cover1)
                .into(binding.bookCoverImageView)

            binding.btnRemove.setOnClickListener {
                onRemoveClick(book)
            }

            binding.root.setOnClickListener {
                onItemClick(book)
            }

            binding.root.setOnClickListener { onItemClick(book) }
            val bookId = "lRvC5g2AX1oPjH8E9qbo"
            fetchBookAuthor(bookId)
        }
        private fun fetchBookAuthor(bookId: String) {
            db.collection("authors").document(bookId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val author = document.getString("nama") ?: "Tidak ada data"
                        binding.bookAuthorTextView.text = author
                        Timber.tag("Firestore").d("Data berhasil diambil: $author")
                    } else {
                        Timber.tag("Firestore").e("Dokumen tidak ditemukan")
                        binding.bookAuthorTextView.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Timber.tag("FirestoreError").e("Gagal mengambil data: ${e.message}")
                    binding.bookAuthorTextView.visibility = View.GONE
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookData>() {
            override fun areItemsTheSame(oldItem: BookData, newItem: BookData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: BookData, newItem: BookData): Boolean {
                return oldItem == newItem
            }
        }
    }
}
