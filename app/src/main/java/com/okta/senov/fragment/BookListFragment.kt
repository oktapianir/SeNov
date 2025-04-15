package com.okta.senov.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.adapter.BookListAdapter
import com.okta.senov.databinding.FragmentBookListBinding
import com.okta.senov.model.Book
import timber.log.Timber

class BookListFragment : Fragment() {
    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var bookAdapter: BookListAdapter
    private val bookList = mutableListOf<Book>()
    private val filteredList = mutableListOf<Book>()

    // Keep track of read counts separately
    private val bookReadCountMap = mutableMapOf<String, Int>()

    // Sort options
    private enum class SortOption {
        SEMUA,
        PALING_BANYAK_DIBACA,
        PALING_SEDIKIT_DIBACA
    }

    private var currentSortOption = SortOption.SEMUA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupUI()
        fetchReadCountData()
    }

    private fun setupAdapter() {
        bookAdapter = BookListAdapter(
            onBookClick = { book ->
                // Navigate to book detail fragment
                val bundle = Bundle().apply {
                    putString("idBook", book.id)
                    putString("titleBook", book.title)
                    putString("nameAuthor", book.authorName)
                    putString("nameCategory", book.category)
                    putString("bookDescription", book.description)
                    putString("fotoUrl", book.image)
                }
                // Navigate to book detail fragment
                findNavController().navigate(
                    R.id.action_bookListFragment_to_detailDataBookFragment,
                    bundle
                )
            },
            onDeleteClick = { book ->
                showDeleteConfirmationDialog(book)
            },
            onEditClick = { book ->
                navigateToEditBook(book)
            }
        )

        binding.bookRecyclerView.adapter = bookAdapter
    }

    private fun setupUI() {
        // Setup back button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBooks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Setup sort button
        binding.sortButton.setOnClickListener { view ->
            showSortOptions(view)
        }
    }

    private fun showSortOptions(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menu.add("Default").setOnMenuItemClickListener {
            currentSortOption = SortOption.SEMUA
            binding.activeSortTextView.text = "Sort: SEMUA"
            sortAndDisplayBooks()
            true
        }
        popupMenu.menu.add("Banyak dibaca").setOnMenuItemClickListener {
            currentSortOption = SortOption.PALING_BANYAK_DIBACA
            binding.activeSortTextView.text = "Sort: Paling Banyak dibaca"
            sortAndDisplayBooks()
            true
        }
        popupMenu.menu.add("Sedikit dibaca").setOnMenuItemClickListener {
            currentSortOption = SortOption.PALING_SEDIKIT_DIBACA
            binding.activeSortTextView.text = "Sort: Paling Sedikit dibaca"
            sortAndDisplayBooks()
            true
        }
        popupMenu.show()
    }

    private fun fetchReadCountData() {
        showLoading(true)

        db.collection("data_read_listened")
            .get()
            .addOnSuccessListener { documents ->
                bookReadCountMap.clear()

                // Aggregate read count by bookId
                for (document in documents) {
                    val bookId = document.getString("bookId") ?: continue
                    val readCount = document.getLong("readCount")?.toInt() ?: 0

                    // Update the map with the new read count
                    bookReadCountMap[bookId] = (bookReadCountMap[bookId] ?: 0) + readCount
                }

                // Now load the books with the read count data
                loadBooks()
            }
            .addOnFailureListener { e ->
                Timber.e("Error loading read data: ${e.message}")
                // Continue loading books even if read data fails
                loadBooks()
            }
    }

    private fun loadBooks() {
        db.collection("Books")
            .get()
            .addOnSuccessListener { documents ->
                bookList.clear()

                for (document in documents) {
                    val id = document.id
                    val title = document.getString("titleBook") ?: ""
                    val coverUrl = document.getString("fotoUrl") ?: ""
                    val authorName = document.getString("nameAuthor") ?: ""
                    val description = document.getString("bookDescription") ?: ""
                    val category = document.getString("nameCategory") ?: ""

                    // Fetch ratings for each book
                    db.collection("ratings")
                        .whereEqualTo("bookId", id)
                        .get()
                        .addOnSuccessListener { ratingDocs ->
                            var totalRating = 0f
                            var count = 0

                            for (ratingDoc in ratingDocs) {
                                val value = ratingDoc.getDouble("rating")?.toFloat() ?: 0f
                                totalRating += value
                                count++
                            }

                            val averageRating = if (count > 0) totalRating / count else 0f

                            val book = Book(
                                id = id,
                                title = title,
                                authorName = authorName,
                                category = category,
                                description = description,
                                image = coverUrl,
                                rating = averageRating
                            )

                            bookList.add(book)

                            // If this is the last book, update UI
                            if (bookList.size == documents.size()) {
                                sortAndDisplayBooks()
                                showLoading(false)
                            }
                        }
                        .addOnFailureListener {
                            Timber.e("Failed to load rating for book $id")
                            val book = Book(
                                id = id,
                                title = title,
                                authorName = authorName,
                                category = category,
                                description = description,
                                image = coverUrl,
                                rating = 0f
                            )

                            bookList.add(book)

                            // If this is the last book, update UI
                            if (bookList.size == documents.size()) {
                                sortAndDisplayBooks()
                                showLoading(false)
                            }
                        }
                }

                // Handle empty case
                if (documents.isEmpty) {
                    showLoading(false)
                    showEmptyState(true)
                }
            }
            .addOnFailureListener { e ->
                Timber.e("Error loading books: ${e.message}")
                showLoading(false)
                showEmptyState(true)
                Toast.makeText(
                    requireContext(),
                    "Failed to load books: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun sortAndDisplayBooks() {
        when (currentSortOption) {
            SortOption.PALING_BANYAK_DIBACA -> {
                // Sort using the separate read count map
                bookList.sortWith { book1, book2 ->
                    val readCount1 = bookReadCountMap[book1.id] ?: 0
                    val readCount2 = bookReadCountMap[book2.id] ?: 0
                    readCount2.compareTo(readCount1) // Descending order
                }
            }
            SortOption.PALING_SEDIKIT_DIBACA -> {
                // Sort using the separate read count map
                bookList.sortWith { book1, book2 ->
                    val readCount1 = bookReadCountMap[book1.id] ?: 0
                    val readCount2 = bookReadCountMap[book2.id] ?: 0
                    readCount1.compareTo(readCount2) // Ascending order
                }
            }
            SortOption.SEMUA -> {
                // Default order, do nothing
            }
        }

        updateUI()
    }

    private fun navigateToEditBook(book: Book) {
        val bundle = Bundle().apply {
            putString("idBook", book.id)
            putString("titleBook", book.title)
            putString("nameAuthor", book.authorName)
            putString("nameCategory", book.category)
            putString("bookDescription", book.description)
            putString("fotoUrl", book.image)
        }

        // Navigate to edit chapter fragment
        findNavController().navigate(
            R.id.action_bookListFragment_to_editBookFragment,
            bundle
        )
    }

    private fun filterBooks(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(bookList)
        } else {
            val searchQuery = query.lowercase()
            filteredList.addAll(bookList.filter {
                it.title.lowercase().contains(searchQuery) ||
                        it.authorName.lowercase().contains(searchQuery) ||
                        it.category.lowercase().contains(searchQuery)
            })
        }

        bookAdapter.submitList(filteredList.toList())
        showEmptyState(filteredList.isEmpty())
    }

    private fun updateUI() {
        // Update the adapter with the data
        filteredList.clear()
        filteredList.addAll(bookList)
        bookAdapter.submitList(filteredList.toList())

        // Show empty state if needed
        showEmptyState(bookList.isEmpty())
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.bookRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.bookRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            binding.bookRecyclerView.visibility = View.GONE
            binding.emptyStateView.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmationDialog(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete ${book.title}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBook(book: Book) {
        db.collection("Books").document(book.id)
            .delete()
            .addOnSuccessListener {
                // Remove from our list and update UI
                val position = bookList.indexOfFirst { it.id == book.id }
                if (position != -1) {
                    bookList.removeAt(position)
                    updateUI()
                }

                Toast.makeText(
                    requireContext(),
                    "${book.title} has been deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Timber.e("Error deleting book: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to delete book: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}