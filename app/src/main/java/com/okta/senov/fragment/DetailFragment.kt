package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.okta.senov.R
import com.okta.senov.databinding.FragmentDetailBinding
import com.okta.senov.model.Book

@Suppress("DEPRECATION")
class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        // Retrieve the passed book argument
        val book = arguments?.getParcelable<Book>("bookArg")

        // Setup the UI with book details
        book?.let { setupBookDetails(it) }

        // Setup back button
        setupBackButton()

        return binding.root
    }

    private fun setupBookDetails(book: Book) {
        binding.apply {
            // Set book cover
            bookCoverImageView.setImageResource(book.coverResourceId)

            // Set book title
            bookTitle.text = book.title

            // Set book subtitle (you might want to customize this)
            bookSubtitle.text = getString(R.string.karya)

            // Set author name
            authorName.text = book.author

            // Set synopsis
            synopsisText.text = book.synopsis

            // Optional: Set up listen/read button
            listenButton.text = getString(R.string.read, book.price)
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
