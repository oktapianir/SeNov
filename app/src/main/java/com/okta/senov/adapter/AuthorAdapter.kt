package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.ItemAuthorBinding
import com.okta.senov.model.Author

class AuthorAdapter(
    private val onAuthorClick: (Author) -> Unit,
    private val onDeleteClick: (Author) -> Unit,
    private val onEditClick: (Author) -> Unit
) : ListAdapter<Author, AuthorAdapter.AuthorViewHolder>(AuthorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val binding = ItemAuthorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AuthorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AuthorViewHolder(private val binding: ItemAuthorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(author: Author) {
            binding.apply {
                authorNameTextView.text = author.nameAuthor
                authorIdTextView.text = author.idAuthor

                // Load author image with Glide
                Glide.with(authorImageView.context)
                    .load(author.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(authorImageView)

                // Item click listener
                root.setOnClickListener {
                    onAuthorClick(author)
                }
                binding.deleteButton.setOnClickListener {
                    onDeleteClick(author) // Panggil callback-nya
                }
                binding.editButton.setOnClickListener{
                    onEditClick(author)
                }
            }
        }
    }
}

class AuthorDiffCallback : DiffUtil.ItemCallback<Author>() {
    override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean {
        return oldItem.idAuthor == newItem.idAuthor
    }

    override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean {
        return oldItem == newItem
    }
}