package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.TopAuthorsAdapter
import com.okta.senov.adapter.TopBooksAdapter
import com.okta.senov.databinding.FragmentSearchBinding
import com.okta.senov.extensions.findNavController
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var authorAdapter: TopAuthorsAdapter
    private lateinit var bookAdapter: TopBooksAdapter
    private lateinit var recentBookAdapter: TopBooksAdapter

    private val viewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        setupAuthorsRecyclerView()
        setupBooksRecyclerView()
        setupRecentBooksRecyclerView()
        setupObservers()

        viewModel.fetchBooksFromApi(getString(R.string.api_key))
        viewModel.fetchTopAuthorsFromFirebase()

        binding.backButton.setOnClickListener {
            binding.backButton.findNavController().navigateUp()
        }
    }

    private fun setupAuthorsRecyclerView() {
        binding.topAuthorsRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        authorAdapter = TopAuthorsAdapter(emptyList())
        binding.topAuthorsRecycler.adapter = authorAdapter
    }

    private fun setupBooksRecyclerView() {
        binding.topBooksRecycler.layoutManager = LinearLayoutManager(context)
        bookAdapter = TopBooksAdapter(emptyList())
        binding.topBooksRecycler.adapter = bookAdapter
    }

    private fun setupRecentBooksRecyclerView() {
        binding.recentBooksRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recentBookAdapter = TopBooksAdapter(emptyList())
        binding.recentBooksRecycler.adapter = recentBookAdapter
    }

    private fun setupObservers() {

        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            Timber.tag("SearchFragment").d("Menerima ${authors.size} authors dari LiveData")
            authorAdapter.updateAuthors(authors)
        }

    }
}
