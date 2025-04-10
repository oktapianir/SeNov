package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.TopAuthorsBinding
import com.okta.senov.model.Author
import timber.log.Timber

// 🔹 Adapter untuk menampilkan daftar penulis (author)
class TopAuthorsAdapter(
    private var authors: List<Author>,                           // 🔹 Data list author
    private val onAuthorClickListener: (Author) -> Unit          // 🔹 Callback saat item diklik
) : RecyclerView.Adapter<TopAuthorsAdapter.AuthorViewHolder>() {

    // 🔹 ViewHolder untuk menyimpan view binding dari item author
    class AuthorViewHolder(val binding: TopAuthorsBinding) : RecyclerView.ViewHolder(binding.root)

    // 🔹 Membuat ViewHolder dari layout XML (TopAuthorsBinding)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val binding = TopAuthorsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AuthorViewHolder(binding)
    }

    // 🔹 Menampilkan data ke dalam setiap ViewHolder
    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        val author = authors[position]

        // 🔹 Set nama penulis
        holder.binding.authorName.text = author.nameAuthor

        // 🔹 Logging nama dan URL gambar author (untuk debug)
        Timber.tag("TopAuthorsAdapter")
            .d("Author: ${author.nameAuthor}, Image URL: ${author.imageUrl}")

        // 🔹 Menampilkan gambar author dengan Glide
        if (author.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(author.imageUrl)
                .error(R.drawable.ic_error)                            // 🔹 Jika gagal load gambar
                .centerCrop()
                .into(holder.binding.authorImage)
        } else {
            // 🔹 Jika URL kosong, tampilkan gambar placeholder
            holder.binding.authorImage.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // 🔹 Listener jika item diklik
        holder.itemView.setOnClickListener {
            onAuthorClickListener(author)
        }
    }

    // 🔹 Jumlah item dalam list
    override fun getItemCount(): Int = authors.size

    // 🔹 Fungsi untuk memperbarui daftar author dan refresh tampilan
    fun updateAuthors(newAuthors: List<Author>) {
        authors = newAuthors
        notifyDataSetChanged()
    }
}
