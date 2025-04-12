package com.okta.senov.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.ChapterAdapter
import com.okta.senov.databinding.FragmentChaptersListBinding
import com.okta.senov.model.BookContent
import com.okta.senov.model.Chapter

class ChapterListFragment : Fragment() {
    private var _binding: FragmentChaptersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var chapterAdapter: ChapterAdapter
    private var bookContent: BookContent? = null
    private val chapterList = mutableListOf<Chapter>()
    private val filteredList = mutableListOf<Chapter>()

    private var bookId: String? = null
    private var bookTitle: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChaptersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get data from arguments
        bookId = arguments?.getString("bookId")
        bookTitle = arguments?.getString("bookTitle")
        bookContent = arguments?.getSerializable("bookContent") as? BookContent

        if (bookId == null || bookContent == null) {
            Toast.makeText(context, "Book information is required", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        binding.addMore.setOnClickListener {
            findNavController().navigate(R.id.addContentBookFragment)
        }

        setupUI()
        setupAdapter()
        loadChapters()
    }

    private fun setupUI() {
        // Set title
        binding.titleTextView.text = "$bookTitle Chapters"

        // Setup back button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup search functionality
        binding.searchEditText.hint = "Search chapters..."
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChapters(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAdapter() {
        chapterAdapter = ChapterAdapter(
            onChapterClick = { chapter ->
                navigateToChapterDetail(chapter)
            }
        )

        binding.bookContentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chapterAdapter
        }
    }

    private fun navigateToChapterDetail(chapter: Chapter) {
        val bundle = Bundle().apply {
            putString("bookId", bookId)
            putString("bookTitle", bookTitle)
            putInt("chapterNumber", chapter.number)
            putString("chapterTitle", chapter.title)
            putString("chapterContent", chapter.content)
        }

        // Navigate to chapter reading fragment
        findNavController().navigate(
            R.id.action_chapterListFragment_to_chapterReadingFragment,
            bundle
        )
    }

    private fun loadChapters() {
        showLoading(true)

        // Use the chapters from the BookContent object
        bookContent?.let { content ->
            chapterList.clear()
            chapterList.addAll(content.chapters.sortedBy { it.number })

            updateUI()
            showLoading(false)
        } ?: run {
            showEmptyState(true)
            showLoading(false)
        }
    }

    private fun filterChapters(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(chapterList)
        } else {
            val searchQuery = query.lowercase()
            filteredList.addAll(chapterList.filter {
                it.title.lowercase().contains(searchQuery)
            })
        }

        chapterAdapter.submitList(filteredList.toList())
        showEmptyState(filteredList.isEmpty())
    }

    private fun updateUI() {
        filteredList.clear()
        filteredList.addAll(chapterList)
        chapterAdapter.submitList(filteredList.toList())

        showEmptyState(chapterList.isEmpty())
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.bookContentRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.bookContentRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            binding.bookContentRecyclerView.visibility = View.GONE
            binding.emptyStateView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}