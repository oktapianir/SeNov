package com.okta.senov.adapter
////
////import android.view.LayoutInflater
////import android.view.ViewGroup
////import androidx.recyclerview.widget.RecyclerView
////import com.bumptech.glide.Glide
////import com.okta.senov.R
////import com.okta.senov.databinding.ItemBookBinding
////import com.okta.senov.model.BookData
////
////class BookAdapter(private val books: List<BookData>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
////
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
////        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
////        return BookViewHolder(binding)
////    }
////
////    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
////        val book = books[position]
////        holder.bind(book)
////    }
////
////    override fun getItemCount() = books.size
////
////    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
////        fun bind(book: BookData) {
////            binding.bookTitleTextView.text = book.title
////            Glide.with(binding.root.context)
////                .load(book.image)
////                .placeholder(R.drawable.img_book_cover1)
////                .into(binding.bookCoverImageView)
////        }
////    }
////}
//
////package com.okta.senov.adapter
////
////import android.view.LayoutInflater
////import android.view.ViewGroup
////import androidx.recyclerview.widget.RecyclerView
////import com.bumptech.glide.Glide
////import com.okta.senov.R
////import com.okta.senov.databinding.ItemBookBinding
////import com.okta.senov.extensions.findNavController
////import com.okta.senov.fragment.HomeFragmentDirections
////import com.okta.senov.model.Book
////import com.okta.senov.model.BookData
////
////class BookAdapter(
////    private val books: List<BookData>,
////    private val onItemClick: (BookData) -> Unit
////) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
////
////    inner class BookViewHolder(
////        private val binding: ItemBookBinding,
////        private val onItemClick: (BookData) -> Unit
////    ) : RecyclerView.ViewHolder(binding.root) {
////
////        fun bind(book: BookData) {
////            binding.apply {
////                bookTitleTextView.text = book.title
////                Glide.with(binding.root.context)
////                    .load(book.image)
////                    .placeholder(R.drawable.img_book_cover1)
////                    .into(bookCoverImageView)
////
////                root.setOnClickListener { onItemClick(book) }
////
////                buttonGoToDetail.setOnClickListener {
////                    val navController = binding.root.findNavController()
////                    val action = HomeFragmentDirections.actionHomeToDetail(
////                        Book(
////                            id = book.id,
////                            title = book.title,
//////                            authorName = book.authorName,
////                            coverResourceId = book.image
////                        )
////                    )
////                    navController.navigate(action)
////                }
////            }
////        }
////    }
////
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
////        val binding = ItemBookBinding.inflate(
////            LayoutInflater.from(parent.context),
////            parent,
////            false
////        )
////        return BookViewHolder(binding, onItemClick)
////    }
////
////    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
////        holder.bind(books[position])
////    }
////
////    override fun getItemCount() = books.size
////
////    fun updateBooks(newBooks: List<BookData>) {
////        val updatedList = books.toMutableList() // Buat salinan list
////        updatedList.addAll(newBooks) // Tambahkan data baru
////        notifyDataSetChanged()
////    }
////}
//
//package com.okta.senov.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.okta.senov.R
//import com.okta.senov.databinding.ItemBookBinding
//import com.okta.senov.extensions.findNavController
//import com.okta.senov.fragment.HomeFragmentDirections
//import com.okta.senov.model.Book
//import com.okta.senov.model.BookData
//
//class BookAdapter(
//    private var books: List<BookData>, // Menggunakan var agar dapat diperbarui
//    private val onItemClick: (BookData) -> Unit
//) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
//
//    inner class BookViewHolder(
//        private val binding: ItemBookBinding,
//        private val onItemClick: (BookData) -> Unit
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(book: BookData) {
//            binding.apply {
//                bookTitleTextView.text = book.title
//                Glide.with(binding.root.context)
//                    .load(book.image)
//                    .placeholder(R.drawable.img_book_cover1)
//                    .into(bookCoverImageView)
//
//                root.setOnClickListener { onItemClick(book) }
//
//                buttonGoToDetail.setOnClickListener {
//                    val navController = binding.root.findNavController()
//                    val action = HomeFragmentDirections.actionHomeToDetail(
//                        Book(
//                            id = book.id,
//                            title = book.title,
////                            authorName = book.authorName,
//                            coverResourceId = book.image
//                        )
//                    )
//                    navController.navigate(action)
//                }
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
//        val binding = ItemBookBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return BookViewHolder(binding, onItemClick)
//    }
//
//    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
//        holder.bind(books[position])
//    }
//
//    override fun getItemCount() = books.size
//
//    // Fungsi untuk memperbarui data di adapter
//    fun updateBooks(newBooks: List<BookData>) {
//        books = newBooks // Mengganti data lama dengan data baru
//        notifyDataSetChanged()
//    }
//}

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
    private val onItemClick: (BookData) -> Unit
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
