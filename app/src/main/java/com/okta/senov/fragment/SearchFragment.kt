package com.okta.senov.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.BookGridAdapter
import com.okta.senov.adapter.TopAuthorsAdapter
import com.okta.senov.adapter.TopBooksAdapter
import com.okta.senov.databinding.FragmentSearchBinding
import com.okta.senov.extensions.findNavController
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var authorAdapter: TopAuthorsAdapter
    private lateinit var bookGridAdapter: BookGridAdapter
    private lateinit var searchResultAdapter: TopBooksAdapter

    private val viewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        setupAuthorsRecyclerView()
        setupBooksGridRecyclerView()
        setupSearchResultRecyclerView()
        setupObservers()
        setupSearchListener()

        viewModel.fetchBooksFromApi(getString(R.string.api_key))
        viewModel.fetchTopAuthorsFromFirebase()
        viewModel.fetchPopularBooksFromFirestore()
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

    private fun setupBooksGridRecyclerView() {
        // Using a Grid Layout with 3 columns and 2 rows to show exactly 6 books
        binding.bookRecycler.layoutManager = GridLayoutManager(
            context,
            3, // Display 3 books per row (will show 2 rows of 3 books each)
            GridLayoutManager.VERTICAL, // Change to vertical so we have rows instead of scrolling horizontally
            false
        )

        bookGridAdapter = BookGridAdapter(emptyList()) { book ->
            // Handle book click - navigate to book details
            val action = SearchFragmentDirections.actionSearchToDetailBook(book)
            findNavController().navigate(action)
        }

        binding.bookRecycler.adapter = bookGridAdapter
        binding.bookRecycler.isNestedScrollingEnabled = false // Prevents scrolling within the grid
    }

    private fun setupSearchResultRecyclerView() {
        binding.searchResultRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        searchResultAdapter = TopBooksAdapter(emptyList(), TopBooksAdapter.ViewType.SEARCH_RESULT)
        binding.searchResultRecycler.adapter = searchResultAdapter
    }

    private fun setupSearchListener() {
        // Add some delay to avoid too many searches while typing
        var searchJob: kotlinx.coroutines.Job? = null

        binding.searchEditText.doAfterTextChanged { text ->
            val query = text.toString().trim()

            // Cancel previous search job if exists
            searchJob?.cancel()

            if (query.isEmpty()) {
                // Show regular content when search is empty
                binding.authorsSection.visibility = View.VISIBLE
                binding.bookSection.visibility = View.VISIBLE
                binding.searchResultRecycler.visibility = View.GONE

                // Clear any previous search results
                searchResultAdapter.updateBooks(emptyList())
            } else {
                // Use coroutines to debounce search input
                searchJob = lifecycleScope.launch {
                    delay(300) // Wait for 300ms after last key press

                    // Hide regular sections and show search results when searching
                    binding.authorsSection.visibility = View.GONE
                    binding.bookSection.visibility = View.GONE
                    binding.searchResultRecycler.visibility = View.VISIBLE

                    // Show loading indicator
                    binding.searchProgressBar?.visibility = View.VISIBLE

                    // Perform search
                    viewModel.searchBooks(query)
                }
            }
        }

        // Add a clear button to the search field
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when search is pressed
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupObservers() {
        // Observe top authors
        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            Timber.tag("SearchFragment").d("Received ${authors.size} authors from LiveData")
            authorAdapter.updateAuthors(authors)
        }

        // Observe popular books
        viewModel.popularBooks.observe(viewLifecycleOwner) { booksData ->
            Timber.tag("SearchFragment").d("Received ${booksData.size} popular books from LiveData")

            // Take only the first 6 books
            val booksToShow = booksData.take(6).map { bookData ->
                com.okta.senov.model.Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    image = bookData.image
                )
            }

            // Pass the converted list to the adapter
            bookGridAdapter.updateBooks(booksToShow)
        }
        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            binding.searchProgressBar?.visibility = View.GONE

            Timber.d("Search Results Observer called with ${searchResults.size} results")
            searchResultAdapter.updateBooks(searchResults)

            // Show message when no results found
            if (searchResults.isEmpty() && binding.searchEditText.text.toString().isNotBlank()) {
                binding.noResultsText?.visibility = View.VISIBLE
                binding.searchResultRecycler.visibility = View.GONE
            } else if (searchResults.isNotEmpty()) {
                binding.noResultsText?.visibility = View.GONE
                binding.searchResultRecycler.visibility = View.VISIBLE
            }
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.searchProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}