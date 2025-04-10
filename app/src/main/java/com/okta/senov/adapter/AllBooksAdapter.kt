package com.okta.senov.adapter

import android.annotation.SuppressLint
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
import com.okta.senov.model.BookContent
import timber.log.Timber

class AllBooksAdapter(
    private var books: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<AllBooksAdapter.AllBooksViewHolder>() {

    // 🔹 Inisialisasi Firestore dan list untuk konten buku serta jumlah rekomendasi
    private val db = FirebaseFirestore.getInstance()
    private var bookContentList: List<BookContent> = emptyList()
    private val recommendationCountMap = mutableMapOf<String, Int>()

    init {
        // 🔹 Ambil data rekomendasi saat adapter dibuat
        fetchAllRecommendationCounts()
    }

    inner class AllBooksViewHolder(
        val binding: ItemBookAllbooksBinding,
        private val onItemClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.apply {
                // 🔹 Tampilkan judul dan genre
                bookTitleTextView.text = book.title
                genreChip.text = book.category

                // 🔹 Tampilkan nama penulis jika tersedia, jika tidak ambil dari Firestore
                if (book.authorName.isNotEmpty()) {
                    bookAuthorTextView.text = book.authorName
                    bookAuthorTextView.visibility = View.VISIBLE
                } else {
                    fetchBookAuthor(book.id)
                }

                // 🔹 Tampilkan cover buku dengan Glide
                if (book.image.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(book.image)
                        .error(R.drawable.ic_book)
                        .into(bookCoverImageView)

                    Timber.tag("IMAGE_LOADING").d("Loading image from URL: ${book.image}")
                } else {
                    Timber.tag("IMAGE_LOADING").e("Empty image URL for book: ${book.title}")
                }

                // 🔹 Tampilkan jumlah rekomendasi
                val recommendCount = recommendationCountMap[book.id] ?: 0
                recommendCountTextView.text = "$recommendCount Rekomendasi"

                // 🔹 Listener item
                root.setOnClickListener { onItemClick(book) }

                // 🔹 Navigasi ke detail saat tombol diklik
                buttonGoToDetail.setOnClickListener {
                    val navController = binding.root.findNavController()
                    val action = HomeFragmentDirections.actionHomeToDetail(book)
                    navController.navigate(action)
                }
            }
        }

        // 🔹 Ambil data penulis dari Firestore jika tidak tersedia
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

    // 🔹 Ambil jumlah rekomendasi untuk semua buku
    private fun fetchAllRecommendationCounts() {
        db.collection("ratings")
            .whereEqualTo("recommended", true)
            .get()
            .addOnSuccessListener { documents ->
                recommendationCountMap.clear()

                for (document in documents) {
                    val bookId = document.getString("bookId") ?: continue
                    val currentCount = recommendationCountMap[bookId] ?: 0
                    recommendationCountMap[bookId] = currentCount + 1
                }

                notifyDataSetChanged()
                Timber.tag("RECOMMENDATIONS").d("Fetched recommendation counts: $recommendationCountMap")
            }
            .addOnFailureListener { e ->
                Timber.tag("RECOMMENDATIONS").e("Error fetching recommendations: ${e.message}")
            }
    }

    // 🔹 Ambil jumlah rekomendasi untuk satu buku (opsional, tidak digunakan)
    private fun fetchRecommendationCount(bookId: String, onComplete: (Int) -> Unit) {
        db.collection("ratings")
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("recommended", true)
            .get()
            .addOnSuccessListener { documents ->
                onComplete(documents.size())
                Timber.tag("RECOMMENDATIONS").d("Book $bookId has ${documents.size()} recommendations")
            }
            .addOnFailureListener { e ->
                Timber.tag("RECOMMENDATIONS").e("Error: ${e.message}")
                onComplete(0)
            }
    }

    // 🔹 Hitung jumlah chapter dari bookContentList
    private fun getChapterCount(bookId: String): Int {
        val bookContent = bookContentList.find { it.bookId == bookId }
        return bookContent?.chapters?.size ?: 0
    }

    // 🔹 Set konten buku dari luar adapter
    fun setBookContentList(content: List<BookContent>) {
        bookContentList = content
        notifyDataSetChanged()
    }

    // 🔹 Buat ViewHolder dari layout XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllBooksViewHolder {
        val binding = ItemBookAllbooksBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AllBooksViewHolder(binding, onItemClick)
    }

    // 🔹 Tampilkan data ke ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AllBooksViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)

        // 🔹 Tampilkan jumlah chapter
        val chapterCount = getChapterCount(book.id)
        holder.binding.chaptersTextView.text = "$chapterCount Chapters"
    }

    // 🔹 Jumlah item
    override fun getItemCount() = books.size

    // 🔹 Perbarui list buku dan rekomendasi
    fun setBooks(newBooks: List<Book>) {
        books = newBooks
        fetchAllRecommendationCounts()
        notifyDataSetChanged()
    }

    // 🔹 Memaksa refresh data rekomendasi
    fun refreshRecommendations() {
        fetchAllRecommendationCounts()
    }
}
