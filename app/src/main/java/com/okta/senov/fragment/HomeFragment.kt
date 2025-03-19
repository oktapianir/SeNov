//package com.okta.senov.fragment
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.okta.senov.R
//import com.okta.senov.adapter.AllBooksAdapter
//import com.okta.senov.adapter.BookAdapter
//import com.okta.senov.databinding.FragmentHomeBinding
//import com.okta.senov.extensions.findNavController
//import com.okta.senov.model.Book
//import com.okta.senov.viewmodel.BookViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import timber.log.Timber
//
//@AndroidEntryPoint
//class HomeFragment : Fragment(R.layout.fragment_home) {
//
//    private lateinit var binding: FragmentHomeBinding
//    private val bookViewModel: BookViewModel by viewModels()
//
//    private val apiKey = "8b71325fbf3a43d8a949fd23ce4e2f5a"
//
//    // Adapter untuk daftar buku populer
//    private lateinit var bookAdapter: BookAdapter
//
//    // Adapter untuk semua buku
//    private lateinit var allBooksAdapter: AllBooksAdapter
//
//    private var allBookList: list<Book> = emptyList()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentHomeBinding.bind(view)
//        setupRecyclerViews()
//
//        // Inisialisasi Adapter
//        bookAdapter = BookAdapter { bookData ->
//            val book = Book(
//                id = bookData.id,
//                title = bookData.title,
//                authorName = bookData.authorName,
//                category = bookData.category,
//                description = bookData.description,
//                image = bookData.image
//            )
//            val action = HomeFragmentDirections.actionHomeToDetail(book)
////            binding.popularBooksRecyclerView.findNavController().navigate(action)
//        }
//
//        allBooksAdapter = AllBooksAdapter(emptyList()) { book ->
//            val action = HomeFragmentDirections.actionHomeToDetail(book)
//            binding.allBooksRecyclerView.findNavController().navigate(action)
//        }
//
//
////        binding.popularBooksRecyclerView.adapter = bookAdapter
//        binding.allBooksRecyclerView.adapter = allBooksAdapter
//
//        // Fetch data dari API
//        bookViewModel.fetchBooksFromApi(apiKey, "adventure")
//
//        // Observasi data populer dan update adapter
//        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
//            bookAdapter.submitList(books)
//        }
//
//        // Observasi semua buku dan update adapter
//        bookViewModel.allBooks.observe(viewLifecycleOwner) { bookDataList ->
//            // Add logging
//            Timber.tag("HOME_FRAGMENT").d("Received ${bookDataList.size} books")
//
//            val books = bookDataList.map { bookData ->
//                // Log each conversion
//                Timber.tag("DATA_CONVERSION")
//                    .d("Converting: ID=${bookData.id}, Title=${bookData.title}")
//
//                Book(
//                    id = bookData.id,
//                    title = bookData.title,
//                    authorName = bookData.authorName,
//                    category = bookData.category,
//                    description = bookData.description,
//                    image = bookData.image
//                )
//            }
//            allBookList = books
//
//            // Log the converted books
//            Timber.tag("ADAPTER_UPDATE").d("Setting ${books.size} books to adapter")
//
//            // Update adapter
//            allBooksAdapter.setBooks(books)
//        }
//        setupSearchFunctionality()
//
//        // Navigasi ke halaman lain
//        binding.profileImage.setOnClickListener {
//            binding.profileImage.findNavController().navigate(R.id.action_home_to_profile)
//        }
//
//        binding.searchIcon.setOnClickListener {
//            binding.searchIcon.findNavController().navigate(R.id.action_home_to_search)
//        }
//
//        binding.yourBook.setOnClickListener {
//            binding.yourBook.findNavController().navigate(R.id.action_home_to_yourbook)
//        }
//
//        setupCategoryChips()
//    }
//
//    private fun setupRecyclerViews() {
////        binding.popularBooksRecyclerView.layoutManager =
////            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        binding.allBooksRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//    }
//
//    private fun setupSearchFunctionality() {
//        binding.searchEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Tidak diperlukan implementasi
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Tidak diperlukan implementasi
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                val searchQuery = s.toString().trim()
//                filterBooks(searchQuery)
//            }
//        })
//    }
//
//    private fun filterBooks(query: String) {
//        Timber.tag("SEARCH").d("Filtering books with query: '$query'")
//
//        if (query.isEmpty()) {
//            // Jika query kosong, tampilkan semua buku
//            allBooksAdapter.setBooks(allBookList)
//            return
//        }
//
//        // Filter buku berdasarkan judul yang mengandung query (case insensitive)
//        val filteredBooks = allBookList.filter {
//            it.title.contains(query, ignoreCase = true)
//        }
//
//        Timber.tag("SEARCH").d("Found ${filteredBooks.size} books matching query")
//
//        // Update adapter dengan hasil pencarian
//        allBooksAdapter.setBooks(filteredBooks)
//
//        // Jika tidak ada hasil, bisa menambahkan tampilan "tidak ada hasil"
//        if (filteredBooks.isEmpty()) {
//            // Jika Anda memiliki TextView untuk pesan "tidak ada hasil", tampilkan di sini
//            // binding.noResultsTextView.visibility = View.VISIBLE
//        } else {
//            // binding.noResultsTextView.visibility = View.GONE
//        }
//    }
//
//    private fun setupCategoryChips() {
//        // Listener untuk chip kategori
//        binding.allChip.setOnClickListener {
//            filterBooksByCategory("all")
//        }
//
//        binding.fictionChip.setOnClickListener {
//            filterBooksByCategory("fiction")
//        }
//
//        binding.romanceChip.setOnClickListener {
//            filterBooksByCategory("romance")
//        }
//
//        binding.mysteryChip.setOnClickListener {
//            filterBooksByCategory("mystery")
//        }
//
//        binding.scienceChip.setOnClickListener {
//            filterBooksByCategory("science fiction")
//        }
//    }
//
//    private fun filterBooksByCategory(category: String) {
//        Timber.tag("CATEGORY").d("Filtering books by category: $category")
//
//        val searchQuery = binding.searchEditText.text.toString().trim()
//
//        val filteredBooks = if (category.equals("all", ignoreCase = true)) {
//            // Jika kategori "all", tampilkan semua buku yang sesuai dengan query pencarian
//            if (searchQuery.isEmpty()) {
//                allBookList
//            } else {
//                allBookList.filter { it.title.contains(searchQuery, ignoreCase = true) }
//            }
//        } else {
//            // Filter berdasarkan kategori dan juga query pencarian jika ada
//            allBookList.filter {
//                it.category.equals(category, ignoreCase = true) &&
//                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
//            }
//        }
//
//        Timber.tag("CATEGORY").d("Found ${filteredBooks.size} books in category")
//
//        // Update adapter dengan hasil filter
//        allBooksAdapter.setBooks(filteredBooks)
//    }
//}

package com.okta.senov.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.AllBooksAdapter
import com.okta.senov.adapter.BookAdapter
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

    // Perbaikan disini: mengganti listOf<Book>() dengan List<Book> = emptyList()
    private var allBookList: List<Book> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        setupRecyclerViews()

        // Inisialisasi Adapter
        bookAdapter = BookAdapter { bookData ->
            val book = Book(
                id = bookData.id,
                title = bookData.title,
                authorName = bookData.authorName,
                category = bookData.category,
                description = bookData.description,
                image = bookData.image
            )
            val action = HomeFragmentDirections.actionHomeToDetail(book)
//            binding.popularBooksRecyclerView.findNavController().navigate(action)
        }

        allBooksAdapter = AllBooksAdapter(emptyList()) { book ->
            val action = HomeFragmentDirections.actionHomeToDetail(book)
            binding.allBooksRecyclerView.findNavController().navigate(action)
        }


//        binding.popularBooksRecyclerView.adapter = bookAdapter
        binding.allBooksRecyclerView.adapter = allBooksAdapter

        // Fetch data dari API
        bookViewModel.fetchBooksFromApi(apiKey, "adventure")

        // Observasi data populer dan update adapter
        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
        }

        // Observasi semua buku dan update adapter
        bookViewModel.allBooks.observe(viewLifecycleOwner) { bookDataList ->
            // Add logging
            Timber.tag("HOME_FRAGMENT").d("Received ${bookDataList.size} books")

            val books = bookDataList.map { bookData ->
                // Log each conversion
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

            // Log the converted books
            Timber.tag("ADAPTER_UPDATE").d("Setting ${books.size} books to adapter")

            // Update adapter
            allBooksAdapter.setBooks(books)
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

        setupCategoryChips()
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
                // Tidak diperlukan implementasi
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
            binding.emptyRecyclerView.visibility = View.GONE
            binding.allBooksSection.visibility = View.VISIBLE
            return
        }

        val filteredBooks = allBookList.filter {
            it.title.contains(query, ignoreCase = true)
        }

        Timber.tag("SEARCH").d("Found ${filteredBooks.size} books matching query")

        // Update adapter dengan hasil pencarian
        allBooksAdapter.setBooks(filteredBooks)

        if (filteredBooks.isEmpty()) {
            binding.emptySection.visibility = View.VISIBLE
            binding.allBooksRecyclerView.visibility = View.GONE
        } else {
             binding.emptySection.visibility = View.GONE
            binding.allBooksRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupCategoryChips() {
        // Listener untuk chip kategori
        binding.allChip.setOnClickListener {
            filterBooksByCategory("all")
        }

        binding.fictionChip.setOnClickListener {
            filterBooksByCategory("fiction")
        }

        binding.romanceChip.setOnClickListener {
            filterBooksByCategory("romance")
        }

        binding.mysteryChip.setOnClickListener {
            filterBooksByCategory("mystery")
        }

        binding.adventureChip.setOnClickListener {
            filterBooksByCategory("adventure")
        }
    }

    private fun filterBooksByCategory(category: String) {
        Timber.tag("CATEGORY").d("Filtering books by category: $category")

        val searchQuery = binding.searchEditText.text.toString().trim()

        // Perbaikan disini: allBooksList -> allBookList
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
    }
}