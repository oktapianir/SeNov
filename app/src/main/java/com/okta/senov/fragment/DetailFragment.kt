package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.okta.senov.R
import com.okta.senov.databinding.FragmentDetailBinding
import com.okta.senov.model.Book

@Suppress("DEPRECATION")
class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val bookDescriptions = mapOf(
        "Book Title 1" to "This is the description for Book Title 1. It provides detailed insights about the content.",
        "Book Title 2" to "This is the description for Book Title 2. It elaborates on the themes and narrative structure.",
        "Book Title 3" to "This is the description for Book Title 3. A fascinating exploration of the genre."
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        val book = arguments?.getParcelable<Book>("bookArg")

        book?.let { setupBookDetails(it) }

        setupBackButton()

        return binding.root
    }

    private fun setupBookDetails(book: Book) {
        binding.apply {
            Glide.with(binding.bookCoverImageView.context)
                .load(book.coverResourceId)
                .into(binding.bookCoverImageView)
            bookTitle.text = book.title
            bookSubtitle.text = getString(R.string.karya)
//            authorName.text = book.author
//            synopsisText.text = book.synopsis
//            listenButton.text = getString(R.string.read, book.price)
        }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )

        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
