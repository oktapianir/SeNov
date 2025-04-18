package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.FragmentDetailAuthorBinding
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailAuthorFragment : Fragment(R.layout.fragment_detail_author) {
    private lateinit var binding: FragmentDetailAuthorBinding
    private val args: DetailAuthorFragmentArgs by navArgs()
    private val viewModel: BookViewModel by viewModels() // Shared ViewModel
    private var isFavorite = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailAuthorBinding.bind(view)

        // Get author ID from navigation arguments
        val authorId = args.author.idAuthor

        // Fetch author details from Firebase
        viewModel.fetchAuthorById(authorId)

        // Check if author is already favorite
        viewModel.isAuthorFavorite(authorId).observe(viewLifecycleOwner) { favorite ->
            isFavorite = favorite
            updateFavoriteButtonUI()
        }

        // Set up observers
        viewModel.selectedAuthor.observe(viewLifecycleOwner) { author ->
            if (author != null) {
                binding.nameAuthor.text = author.nameAuthor
                binding.socialMediaAuthor.text = author.socialMedia
                binding.biographyTextView.text = author.bioAuthor

                // Load author image
                if (author.imageUrl.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(author.imageUrl)
                        .error(R.drawable.ic_error)
                        .centerCrop()
                        .into(binding.authorCoverImageView)
                }
            }
        }

        // Set up back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        // Set up favorite button
        binding.favoriteButton.setOnClickListener {
            val currentAuthor = viewModel.selectedAuthor.value
            if (currentAuthor != null) {
                if (isFavorite) {
                    viewModel.removeAuthorFromFavorites(currentAuthor.idAuthor)
                    Toast.makeText(requireContext(), "Berhasil menghapus favorite", Toast.LENGTH_SHORT).show()

                } else {
                    viewModel.addAuthorToFavorites(currentAuthor)
                    Toast.makeText(requireContext(), "Berhasil menambahkan favorite", Toast.LENGTH_SHORT).show()
                }
                // Toggle state (UI will update via observer)
                isFavorite = !isFavorite
                updateFavoriteButtonUI()
            }
        }
    }
    private fun updateFavoriteButtonUI() {
        // Change drawable based on favorite status
        val iconResource = if (isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite
        }
        binding.favoriteButton.setImageResource(iconResource)
    }
}