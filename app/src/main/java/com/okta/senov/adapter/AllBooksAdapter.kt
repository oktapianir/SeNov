//package com.okta.senov.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.okta.senov.databinding.ItemBookAllbooksBinding
//import com.okta.senov.extensions.findNavController
//import com.okta.senov.fragment.HomeFragmentDirections
//import com.okta.senov.model.Book
//
//class AllBooksAdapter(
//    private val books: List<Book>,
//    private val onItemClick: (Book) -> Unit
//) : RecyclerView.Adapter<AllBooksAdapter.AllBooksViewHolder>() {
//
//    inner class AllBooksViewHolder(
//        private val binding: ItemBookAllbooksBinding,
//        private val onItemClick: (Book) -> Unit
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(book: Book) {
//            binding.apply {
//                bookTitleTextView.text = book.title
//                Glide.with(binding.root.context)
//                    .load(book.coverResourceId)
//                    .into(bookCoverImageView)
//
//                root.setOnClickListener { onItemClick(book) }
//
//                buttonGoToDetail.setOnClickListener {
//                    val navController = binding.root.findNavController()
//                    val action = HomeFragmentDirections.actionHomeToDetail(book)
//                    navController.navigate(action)
//                }
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllBooksViewHolder {
//        val binding = ItemBookAllbooksBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return AllBooksViewHolder(binding, onItemClick)
//    }
//
//    override fun onBindViewHolder(holder: AllBooksViewHolder, position: Int) {
//        holder.bind(books[position])
//    }
//
//    override fun getItemCount() = books.size
//}


package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.ItemBookAllbooksBinding
import com.okta.senov.extensions.findNavController
import com.okta.senov.fragment.HomeFragmentDirections
import com.okta.senov.model.Book
import timber.log.Timber

class AllBooksAdapter(
    private var books: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<AllBooksAdapter.AllBooksViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class AllBooksViewHolder(
        private val binding: ItemBookAllbooksBinding,
        private val onItemClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        //        fun bind(book: Book) {
//            binding.apply {
//                bookTitleTextView.text = book.title
//                Glide.with(binding.root.context)
//                    .load(book.coverResourceId)
//                    .into(bookCoverImageView)
//
//                root.setOnClickListener { onItemClick(book) }
//
//                buttonGoToDetail.setOnClickListener {
//                    val navController = binding.root.findNavController()
//                    val action = HomeFragmentDirections.actionHomeToDetail(book)
//                    navController.navigate(action)
//                }
//            }
//            val bookId = "lRvC5g2AX1oPjH8E9qbo"
//            fetchBookAuthor(bookId)
//        }
// In AllBooksAdapter.AllBooksViewHolder.bind method
        fun bind(book: Book) {
            binding.apply {
                bookTitleTextView.text = book.title
                genreChip.text = book.category
                // Display author name
                if (book.authorName.isNotEmpty()) {
                    bookAuthorTextView.text = book.authorName
                    bookAuthorTextView.visibility = View.VISIBLE
                } else {
                    // Only fetch from Firestore if we don't have author data
                    fetchBookAuthor(book.id)
                }

                // Make sure Glide is loading the image properly
                if (book.image.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(book.image)
                        .placeholder(R.drawable.ic_add) // Add a placeholder image
                        .error(R.drawable.ic_book) // Add an error image
                        .into(bookCoverImageView)

                    // Log image URL for debugging
                    Timber.tag("IMAGE_LOADING").d("Loading image from URL: ${book.image}")
                } else {
                    Timber.tag("IMAGE_LOADING").e("Empty image URL for book: ${book.title}")
                }

                root.setOnClickListener { onItemClick(book) }

                buttonGoToDetail.setOnClickListener {
                    val navController = binding.root.findNavController()
                    val action = HomeFragmentDirections.actionHomeToDetail(book)
                    navController.navigate(action)
                }
            }
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

    // Tambahkan metode untuk memperbarui daftar buku
    fun setBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
