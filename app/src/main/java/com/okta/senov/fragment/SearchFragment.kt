//package com.okta.senov.fragment
//
//import android.os.Bundle
//import android.view.View
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.okta.senov.R
//import com.okta.senov.adapter.TopAuthorsAdapter
//import com.okta.senov.adapter.TopBooksAdapter
//import com.okta.senov.databinding.FragmentSearchBinding
//import com.okta.senov.extensions.findNavController
//import com.okta.senov.viewmodel.BookViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import timber.log.Timber
//
//@AndroidEntryPoint
//class SearchFragment : Fragment(R.layout.fragment_search) {
//    private lateinit var binding: FragmentSearchBinding
//    private lateinit var authorAdapter: TopAuthorsAdapter
//    private lateinit var recentBookAdapter: TopBooksAdapter
//
//    private val viewModel: BookViewModel by viewModels()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentSearchBinding.bind(view)
//
//        setupAuthorsRecyclerView()
//        setupRecentBooksRecyclerView()
//        setupObservers()
//
//        viewModel.fetchBooksFromApi(getString(R.string.api_key))
//        viewModel.fetchTopAuthorsFromFirebase()
//
//        binding.backButton.setOnClickListener {
//            binding.backButton.findNavController().navigateUp()
//        }
//    }
//
//    private fun setupAuthorsRecyclerView() {
//        binding.topAuthorsRecycler.layoutManager = LinearLayoutManager(
//            context,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        authorAdapter = TopAuthorsAdapter(emptyList()) { author ->
//            // Handle the click here
//            val action = SearchFragmentDirections.actionSearchToDetailAuthor(author)
//            findNavController().navigate(action)
//
//        }
//        binding.topAuthorsRecycler.adapter = authorAdapter
//    }
//
//
//    private fun setupRecentBooksRecyclerView() {
//        binding.recentBooksRecycler.layoutManager = LinearLayoutManager(
//            context,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        recentBookAdapter = TopBooksAdapter(emptyList())
//        binding.recentBooksRecycler.adapter = recentBookAdapter
//    }
//
//    private fun setupObservers() {
//
//        viewModel.authors.observe(viewLifecycleOwner) { authors ->
//            Timber.tag("SearchFragment").d("Menerima ${authors.size} authors dari LiveData")
//            authorAdapter.updateAuthors(authors)
//        }
//
//    }
//}

package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
    private lateinit var recentBookAdapter: TopBooksAdapter
    private lateinit var searchResultAdapter: TopBooksAdapter

    private val viewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        setupAuthorsRecyclerView()
//        setupRecentBooksRecyclerView()
        setupSearchResultRecyclerView()
        setupObservers()
        setupSearchListener()

        viewModel.fetchBooksFromApi(getString(R.string.api_key))
        viewModel.fetchTopAuthorsFromFirebase()
        viewModel.diagnosticFirestoreCheck()


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
        authorAdapter = TopAuthorsAdapter(emptyList()) { author ->
            val action = SearchFragmentDirections.actionSearchToDetailAuthor(author)
            findNavController().navigate(action)
        }
        binding.topAuthorsRecycler.adapter = authorAdapter
    }

//    private fun setupRecentBooksRecyclerView() {
//        binding.recentBooksRecycler.layoutManager = LinearLayoutManager(
//            context,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        recentBookAdapter = TopBooksAdapter(emptyList())
//        binding.recentBooksRecycler.adapter = recentBookAdapter
//    }

    private fun setupSearchResultRecyclerView() {
        binding.searchResultRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        searchResultAdapter = TopBooksAdapter(emptyList())
        binding.searchResultRecycler.adapter = searchResultAdapter
    }

    private fun setupSearchListener() {
        binding.searchEditText.doAfterTextChanged { text ->
            val query = text.toString().trim()

            // Reset visibility of sections
            binding.authorsSection.visibility = View.VISIBLE
//            binding.recentBooksSection.visibility = View.VISIBLE
            binding.searchResultRecycler.visibility = View.GONE

            if (query.isNotEmpty()) {
                // Hide authors and recent books when searching
                binding.authorsSection.visibility = View.VISIBLE
//                binding.recentBooksSection.visibility = View.GONE

                // Perform search
                viewModel.searchBooks(query)
            }
        }
    }

    private fun setupObservers() {
        // Observe top authors
        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            Timber.tag("SearchFragment").d("Received ${authors.size} authors from LiveData")
            authorAdapter.updateAuthors(authors)
        }

        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            Timber.d("Search Results Observer called with ${searchResults.size} results")
            if (searchResults.isNotEmpty()) {
                searchResultAdapter.updateBooks(searchResults)
                binding.searchResultRecycler.visibility = View.VISIBLE
            } else {
                binding.searchResultRecycler.visibility = View.GONE
            }
        }
    }
}
