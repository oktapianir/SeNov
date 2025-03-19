package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.TopAuthorsBinding
import com.okta.senov.model.Author

class TopAuthorsAdapter(private val authors: List<Author>) :
    RecyclerView.Adapter<TopAuthorsAdapter.AuthorViewHolder>() {

    class AuthorViewHolder(private val binding: TopAuthorsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(author: Author) {
//            binding.authorImage.setImageResource(author.imageAuthor)
            Glide.with(binding.authorImage.context)
                .load(author.imageAuthor)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_error)
                .into(binding.authorImage)

            binding.authorName.text = author.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val binding = TopAuthorsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AuthorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        holder.bind(authors[position])
    }

    override fun getItemCount(): Int = authors.size
}
