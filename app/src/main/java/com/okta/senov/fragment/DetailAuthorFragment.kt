package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailAuthorBinding.bind(view)

        // Get author ID from navigation arguments
        val authorId = args.author.id

        // Fetch author details from Firebase
        viewModel.fetchAuthorById(authorId)

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
    }
}