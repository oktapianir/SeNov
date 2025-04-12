package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.databinding.ItemBookContentListBinding
import com.okta.senov.model.Chapter

class ChapterAdapter(
    private val onChapterClick: (Chapter) -> Unit,
    private val onEditClick: (Chapter) -> Unit
) : ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder>(ChapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemBookContentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChapterViewHolder(private val binding: ItemBookContentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: Chapter) {
            binding.apply {
                // Display chapter information
                bookNameTextView.text = chapter.title
                chaptersTextView.text = "Chapter ${chapter.number}"

                // Hide the book image card
                bookImageCard.visibility = ViewGroup.VISIBLE

                // Hide delete button for chapters
                deleteButton.visibility = ViewGroup.VISIBLE

                // Add visibility and click listener for edit button
                editButton.visibility = ViewGroup.VISIBLE
                editButton.setOnClickListener {
                    onEditClick(chapter)
                }

                // Set click listener
                root.setOnClickListener {
                    onChapterClick(chapter)
                }
            }
        }
    }

    class ChapterDiffCallback : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem == newItem
        }
    }
}