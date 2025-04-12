package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.okta.senov.databinding.FragmentChapterReadingBinding

class ChapterReadingFragment : Fragment() {
    private var _binding: FragmentChapterReadingBinding? = null
    private val binding get() = _binding!!

    private var bookTitle: String? = null
    private var chapterTitle: String? = null
    private var chapterContent: String? = null
    private var chapterNumber: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChapterReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get data from arguments
        bookTitle = arguments?.getString("bookTitle")
        chapterTitle = arguments?.getString("chapterTitle")
        chapterContent = arguments?.getString("chapterContent")
        chapterNumber = arguments?.getInt("chapterNumber") ?: 0

        setupUI()
    }

    private fun setupUI() {
        // Set chapter title
        binding.chapterTitleTextView.text = chapterTitle

        // Set book title in toolbar
        binding.bookTitleTextView.text = bookTitle

        // Set chapter number
        binding.chapterNumberTextView.text = "Chapter ${chapterNumber}"

        // Set chapter content
        binding.chapterContentTextView.text = chapterContent

        // Setup back button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}