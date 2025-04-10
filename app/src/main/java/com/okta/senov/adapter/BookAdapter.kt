package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookBinding
import com.okta.senov.model.BookData
import timber.log.Timber

class BookAdapter(
    private val onItemClick: (BookData) -> Unit,
    private val onRemoveClick: (BookData) -> Unit
) : ListAdapter<BookData, BookAdapter.BookViewHolder>(DIFF_CALLBACK) {

    // 🔹 Inisialisasi Firebase Firestore dan Auth
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 🔹 Fungsi untuk mengisi data ke setiap item
        fun bind(book: BookData) {
            // 🔹 Set judul buku
            binding.bookTitleTextView.text = book.title

            // 🔹 Load gambar buku menggunakan Glide
            Glide.with(binding.root.context)
                .load(book.image)
                .placeholder(R.drawable.img_book_cover1)  // placeholder saat loading
                .error(R.drawable.img_book_cover1)        // jika gagal load
                .into(binding.bookCoverImageView)

            // 🔹 Tombol untuk menghapus buku dari daftar
            binding.btnRemove.setOnClickListener {
                onRemoveClick(book)
            }

            // 🔹 Listener untuk klik pada item buku
            binding.root.setOnClickListener {
                onItemClick(book)
            }

            // 🔹 Tampilkan nama penulis
            binding.bookAuthorTextView.text = book.authorName

            // 🔹 Ambil dan tampilkan progress membaca dari Firestore
            fetchReadingProgress(book.id)
        }

        // 🔹 Ambil progress membaca buku untuk user saat ini dari Firestore
        private fun fetchReadingProgress(bookId: String) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                db.collection("users").document(userId)
                    .collection("reading_progress").document(bookId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // 🔹 Jika data ada, ambil persentase progress
                            val readingPercentage = document.getDouble("percentage") ?: 0.0
                            binding.readPercentageTextView.text =
                                String.format("%.1f%%", readingPercentage)
                            Timber.tag("ReadingProgress")
                                .d("Progress for book $bookId: $readingPercentage%%")
                        } else {
                            // 🔹 Jika tidak ada, tampilkan 0.0%
                            binding.readPercentageTextView.text = "0.0%"
                            Timber.tag("ReadingProgress")
                                .d("No progress found for book $bookId")
                        }
                    }
                    .addOnFailureListener { e ->
                        // 🔹 Jika error, tampilkan 0.0% dan log error
                        binding.readPercentageTextView.text = "0.0%"
                        Timber.tag("ReadingProgressError")
                            .e("Error fetching reading progress: ${e.message}")
                    }
            } else {
                // 🔹 Jika user belum login
                binding.readPercentageTextView.text = "0.0%"
            }
        }
    }

    // 🔹 Membuat ViewHolder baru dari layout XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    // 🔹 Memasukkan data ke dalam ViewHolder berdasarkan posisi
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        // 🔹 DiffUtil untuk membandingkan data secara efisien saat list diperbarui
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookData>() {
            override fun areItemsTheSame(oldItem: BookData, newItem: BookData): Boolean {
                // 🔹 Bandingkan berdasarkan ID
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: BookData, newItem: BookData): Boolean {
                // 🔹 Bandingkan seluruh isi item
                return oldItem == newItem
            }
        }
    }
}
