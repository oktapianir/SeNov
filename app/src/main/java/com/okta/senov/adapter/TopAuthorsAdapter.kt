package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.TopAuthorsBinding
import com.okta.senov.model.Author
import timber.log.Timber

class TopAuthorsAdapter(
    private var authors: List<Author>,
    private val onAuthorClickListener: (Author) -> Unit
) :
    RecyclerView.Adapter<TopAuthorsAdapter.AuthorViewHolder>() {

    class AuthorViewHolder(val binding: TopAuthorsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val binding = TopAuthorsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AuthorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        val author = authors[position]

        holder.binding.authorName.text = author.nameAuthor
        Timber.tag("TopAuthorsAdapter")
            .d("Author: ${author.nameAuthor}, Image URL: ${author.imageUrl}")

        // Load image using Glide
        if (author.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(author.imageUrl)
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(holder.binding.authorImage)
        } else {
            holder.binding.authorImage.setImageResource(R.drawable.ic_profile_placeholder)
        }
        holder.itemView.setOnClickListener{
            onAuthorClickListener(author)
        }
    }

    override fun getItemCount(): Int = authors.size

    fun updateAuthors(newAuthors: List<Author>) {
        authors = newAuthors
        notifyDataSetChanged()
    }
}