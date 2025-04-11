package com.okta.senov.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
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
        loadBooks()
    }

    private fun setupAdapter() {
        bookAdapter = BookListAdapter(
            onBookClick = { book ->
                // Handle book click - navigate to book detail
                Toast.makeText(requireContext(), "Selected: ${book.title}", Toast.LENGTH_SHORT).show()
                // You can navigate to another fragment to show book details here
            },
            onDeleteClick = { book ->
                showDeleteConfirmationDialog(book)
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
    }

//    private fun loadBooks() {
//        showLoading(true)
//
//        db.collection("Books")
//            .get()
//            .addOnSuccessListener { documents ->
//                bookList.clear()
//
//                for (document in documents) {
//                    try {
//                        val id = document.id
//                        val title = document.getString("titleBook") ?: ""
//                        val coverUrl = document.getString("fotoUrl") ?: ""
//                        val authorName = document.getString("nameAuthor") ?: ""
//                        val description = document.getString("bookDescription") ?: ""
//                        val category = document.getString("nameCategory") ?: ""
//                        val book = Book(id, title, coverUrl, authorName, description, category)
//                        bookList.add(book)
//                    } catch (e: Exception) {
//                        Timber.e("Error parsing book: ${e.message}")
//                    }
//                }
//
//                // Update UI with the data
//                updateUI()
//                showLoading(false)
//            }
//            .addOnFailureListener { e ->
//                Timber.e("Error loading books: ${e.message}")
//                showLoading(false)
//                showEmptyState(true)
//                Toast.makeText(
//                    requireContext(),
//                    "Failed to load books: ${e.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//    }

    private fun loadBooks() {
        showLoading(true)

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

                    // Ambil rating dari collection Ratings berdasarkan bookId
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
                            updateUI()
                            showLoading(false)
                        }
                        .addOnFailureListener {
                            Timber.e("Failed to load rating for book $id")
                            val book = Book(id, title, authorName, category, description, coverUrl, 0f)
                            bookList.add(book)
                            updateUI()
                            showLoading(false)
                        }
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