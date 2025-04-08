package com.okta.senov.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.okta.senov.R
import com.okta.senov.adapter.AllBooksAdapter
import com.okta.senov.adapter.BookAdapter
import com.okta.senov.databinding.DialogFilterSortBinding
import com.okta.senov.databinding.FragmentHomeBinding
import com.okta.senov.extensions.findNavController
import com.okta.senov.model.Book
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val bookViewModel: BookViewModel by viewModels()

    private val apiKey = "8b71325fbf3a43d8a949fd23ce4e2f5a"

    // Adapter untuk daftar buku populer
    private lateinit var bookAdapter: BookAdapter

    // Adapter untuk semua buku
    private lateinit var allBooksAdapter: AllBooksAdapter

    private var allBookList: List<Book> = emptyList()
    private val SORT_NONE = 0
    private val SORT_HIGH_TO_LOW = 1
    private val SORT_LOW_TO_HIGH = 2
    private var currentSortMode = SORT_NONE


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        setupRecyclerViews()
        binding.emptySection.visibility = View.GONE
        binding.allBooksSection.visibility = View.VISIBLE

        bookAdapter = BookAdapter(
            onItemClick = { bookData ->
                val book = Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    description = bookData.description,
                    image = bookData.image
                )
                val action = HomeFragmentDirections.actionHomeToDetail(book)
                findNavController().navigate(action)
            },
            onRemoveClick = { book ->
            }
        )


        allBooksAdapter = AllBooksAdapter(emptyList()) { book ->
            val action = HomeFragmentDirections.actionHomeToDetail(book)
            binding.allBooksRecyclerView.findNavController().navigate(action)
        }


//        binding.popularBooksRecyclerView.adapter = bookAdapter
        binding.allBooksRecyclerView.adapter = allBooksAdapter

        bookViewModel.fetchBooksFromFirestore()

        // Fetch data dari API
        bookViewModel.fetchBooksFromApi(apiKey, "adventure")

        // Observasi data populer dan update adapter
        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
        }

        bookViewModel.bookContent.observe(viewLifecycleOwner) { contentList ->
            allBooksAdapter.setBookContentList(contentList)
        }

        // Observasi semua buku dan update adapter
        bookViewModel.allBooks.observe(viewLifecycleOwner) { bookDataList ->
            Timber.tag("HOME_FRAGMENT").d("Received ${bookDataList.size} books")

            val books = bookDataList.map { bookData ->
                Timber.tag("DATA_CONVERSION")
                    .d("Converting: ID=${bookData.id}, Title=${bookData.title}")

                Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    description = bookData.description,
                    image = bookData.image
                )
            }
            // Simpan daftar buku ke variabel allBookList
            allBookList = books

            Timber.tag("ADAPTER_UPDATE").d("Setting ${books.size} books to adapter")

            // Update adapter
            allBooksAdapter.setBooks(books)
        }
        bookViewModel.bookRatings.observe(viewLifecycleOwner) { ratings ->
            Timber.d("Ratings updated: ${ratings.size} items")
            // Re-sort jika sudah ada filter yang aktif
            if (currentSortMode != SORT_NONE) {
                sortBooksByRating(currentSortMode == SORT_HIGH_TO_LOW)
            }
        }
        setupSearchFunctionality()

        // Navigasi ke halaman lain
        binding.profileImage.setOnClickListener {
            binding.profileImage.findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.findNavController().navigate(R.id.action_home_to_search)
        }

        binding.yourBook.setOnClickListener {
            binding.yourBook.findNavController().navigate(R.id.action_home_to_yourbook)
        }
        // click listener for filter icon
        binding.filterIcon.setOnClickListener {
            showFilterDialog()
        }

        setupCategoryChips()
        bookViewModel.fetchAllBookContents()
        bookViewModel.fetchRatingsFromFirestore()
    }

    private fun setupRecyclerViews() {
//        binding.popularBooksRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.allBooksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tidak diperlukan implementasi
            }

            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s.toString().trim()
                filterBooks(searchQuery)
            }
        })
    }

    private fun filterBooks(query: String) {
        Timber.tag("SEARCH").d("Filtering books with query: '$query'")

        if (query.isEmpty()) {
            allBooksAdapter.setBooks(allBookList)
            binding.emptySection.visibility = View.GONE
            binding.allBooksSection.visibility = View.VISIBLE
            return
        }

        // Filter books based on title containing the query
        val filteredBooks = allBookList.filter {
            it.title.contains(query, ignoreCase = true)
        }

        Timber.tag("SEARCH").d("Found ${filteredBooks.size} books matching query")

        // Update adapter with search results
        allBooksAdapter.setBooks(filteredBooks)

        // Show empty state if no results, hide otherwise
        if (filteredBooks.isEmpty()) {
            binding.emptySection.visibility = View.VISIBLE
            binding.allBooksSection.visibility = View.GONE
        } else {
            binding.emptySection.visibility = View.GONE
            binding.allBooksSection.visibility = View.VISIBLE
        }
    }

    private fun setupCategoryChips() {
        // Listener untuk chip kategori
        binding.allChip.setOnClickListener {
            filterBooksByCategory("all")
        }

        binding.fictionChip.setOnClickListener {
            filterBooksByCategory("fiksi")
        }

        binding.romanceChip.setOnClickListener {
            filterBooksByCategory("romantis")
        }

        binding.mysteryChip.setOnClickListener {
            filterBooksByCategory("misteri")
        }

        binding.adventureChip.setOnClickListener {
            filterBooksByCategory("adventure")
        }
    }

    private fun filterBooksByCategory(category: String) {
        Timber.tag("CATEGORY").d("Filtering books by category: $category")

        val searchQuery = binding.searchEditText.text.toString().trim()

        val filteredBooks = if (category.equals("all", ignoreCase = true)) {
            // Jika kategori "all", tampilkan semua buku yang sesuai dengan query pencarian
            if (searchQuery.isEmpty()) {
                allBookList
            } else {
                allBookList.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }
        } else {
            // Filter berdasarkan kategori dan juga query pencarian jika ada
            allBookList.filter {
                it.category.equals(category, ignoreCase = true) &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
            }
        }

        Timber.tag("CATEGORY").d("Found ${filteredBooks.size} books in category")

        // Update adapter dengan hasil filter
        allBooksAdapter.setBooks(filteredBooks)
        if (filteredBooks.isEmpty()) {
            binding.emptySection.visibility = View.VISIBLE
            binding.allBooksSection.visibility = View.GONE
        } else {
            binding.emptySection.visibility = View.GONE
            binding.allBooksSection.visibility = View.VISIBLE
        }
    }

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogFilterSortBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Check if ratings are available
        val ratingsAvailable = bookViewModel.bookRatings.value?.isNotEmpty() == true

        if (!ratingsAvailable) {
            Timber.d("Ratings not available yet, showing toast")
            Toast.makeText(requireContext(), "Please wait, loading ratings...", Toast.LENGTH_SHORT)
                .show()
            // Make sure to fetch ratings
            bookViewModel.fetchRatingsFromFirestore()
        }

        dialogBinding.btnApplyFilter.setOnClickListener {
            val selectedRadioButtonId = dialogBinding.radioGroupSort.checkedRadioButtonId
            when (selectedRadioButtonId) {
                R.id.radioHighestRating -> sortBooksByRating(true)
                R.id.radioLowestRating -> sortBooksByRating(false)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sortBooksByRating(highToLow: Boolean) {
        // Update the current sort mode
        currentSortMode = if (highToLow) SORT_HIGH_TO_LOW else SORT_LOW_TO_HIGH

        // Get ratings data
        val ratingsMap = bookViewModel.bookRatings.value

        // Log all available ratings
        Timber.d("Available ratings: ${ratingsMap?.size ?: 0}")
        ratingsMap?.forEach { (bookId, rating) ->
            Timber.d("Rating for book $bookId: $rating")
        }

        // Log all book IDs to verify match
        Timber.d("Available books: ${allBookList.size}")
        allBookList.forEach { book ->
            Timber.d("Book ID: ${book.id}, Title: ${book.title}")
        }

        // Check if we have any non-zero ratings
        val hasValidRatings = ratingsMap?.any { it.value > 0f } == true

        if (!hasValidRatings) {
            Toast.makeText(requireContext(), "No ratings available for sorting", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Create sorted list
        val sortedList = ArrayList(allBookList)

        // Sort based on ratings (with fallback for missing ratings)
        if (highToLow) {
            sortedList.sortByDescending { book -> ratingsMap.get(book.id) ?: 0f }
        } else {
            sortedList.sortBy { book -> ratingsMap.get(book.id) ?: 0f }
        }

        // Update adapter with sorted list
        allBooksAdapter.setBooks(sortedList)

        // Debug log all books with their ratings
        sortedList.forEachIndexed { index, book ->
            val rating = ratingsMap?.get(book.id) ?: 0f
            Timber.d("Sorted book $index: ${book.title} - ID: ${book.id} - Rating: $rating")
        }

        // Scroll to top
        binding.allBooksRecyclerView.scrollToPosition(0)

//        Toast.makeText(
//            requireContext(),
//            "Books sorted by ${if (highToLow) "highest" else "lowest"} rating",
//            Toast.LENGTH_SHORT
//        ).show()
    }
}