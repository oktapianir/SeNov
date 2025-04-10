package com.okta.senov.adapter

// Import yang dibutuhkan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.databinding.ItemYourBookBinding
import com.okta.senov.model.Book

// Adapter untuk menampilkan daftar buku yang dimiliki user
class YourBookAdapter(
    private val onItemClick: (Book) -> Unit, // Callback ketika item diklik
    private val onProgressUpdate: (Book, Int) -> Unit // Callback untuk update progress baca
) : ListAdapter<Book, YourBookAdapter.BookViewHolder>(BookDiffCallback()) {

    // Membuat ViewHolder untuk setiap item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemYourBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    // Menghubungkan data dengan ViewHolder
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder untuk item buku
    inner class BookViewHolder(private val binding: ItemYourBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk mengisi data ke dalam tampilan item
        fun bind(book: Book) {
            binding.apply {
                // Menampilkan judul buku
                bookTitleTextView.text = book.title

                // Menampilkan gambar buku menggunakan Glide
                Glide.with(bookCoverImageView.context)
                    .load(book.image)
                    .into(bookCoverImageView)

                // Listener ketika item diklik
                root.setOnClickListener { onItemClick(book) }
            }
        }
    }

    // DiffUtil untuk efisiensi update RecyclerView
    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id // Bandingkan berdasarkan ID
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem // Bandingkan isi data
        }
    }

  // menmabhakna listener untuk tombol hapus
//    fun setOnRemoveClickListener(listener: (Book) -> Unit) {
//        removeClickListener = listener
//    }
}
