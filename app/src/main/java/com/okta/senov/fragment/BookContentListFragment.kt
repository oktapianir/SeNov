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
import com.okta.senov.R
import com.okta.senov.adapter.BookContentListAdapter
import com.okta.senov.databinding.FragmentBookContentListBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import com.okta.senov.model.Chapter
import timber.log.Timber

class BookContentListFragment : Fragment() {
    private var _binding: FragmentBookContentListBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var bookContentAdapter: BookContentListAdapter
    private val bookContentList = mutableListOf<Pair<Book, BookContent>>()
    private val filteredList = mutableListOf<Pair<Book, BookContent>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookContentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupUI()
        loadBooks()
    }

    private fun setupAdapter() {
        bookContentAdapter = BookContentListAdapter(
            onBookClick = { book, bookContent ->
                // Navigate to chapter list fragment
                navigateToChapterList(book, bookContent)
            },
            onDeleteClick = { book ->
                showDeleteConfirmationDialog(book)
            }
        )

        binding.bookContentRecyclerView.adapter = bookContentAdapter
    }

    private fun navigateToChapterList(book: Book, bookContent: BookContent) {
        // Pass book info to chapter list
        val bundle = Bundle().apply {
            putString("bookId", book.id)
            putString("bookTitle", book.title)
            // Pass the serializable BookContent object
            putSerializable("bookContent", bookContent)
        }

        // Navigate to chapters fragment
        findNavController().navigate(
            R.id.action_bookContentListFragment_to_chapterListFragment,
            bundle
        )
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

    private fun loadBooks() {
        showLoading(true)

        db.collection("Books")
            .get()
            .addOnSuccessListener { documents ->
                bookContentList.clear()

                for (document in documents) {
                    val id = document.id
                    val title = document.getString("titleBook") ?: ""
                    val coverUrl = document.getString("fotoUrl") ?: ""
                    val authorName = document.getString("nameAuthor") ?: ""
                    val description = document.getString("bookDescription") ?: ""
                    val category = document.getString("nameCategory") ?: ""

                    // Create Book object
                    val book = Book(
                        id = id,
                        title = title,
                        authorName = authorName,
                        category = category,
                        description = description,
                        image = coverUrl,
                        rating = 0f
                    )

                    // Load chapters for this book
                    loadBookContent(book)
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

    private fun loadBookContent(book: Book) {
        // Load chapters for this book from the BookContent collection
        db.collection("bookContents")
            .whereEqualTo("bookId", book.id)
            .get()
            .addOnSuccessListener { contentDocs ->
                if (contentDocs.isEmpty) {
                    // If no BookContent exists, create an empty one
                    val emptyBookContent = BookContent(book.id, book.title, emptyList())
                    bookContentList.add(Pair(book, emptyBookContent))
                } else {
                    // Get the first document (should be only one per book)
                    val contentDoc = contentDocs.documents[0]

                    // Parse chapters
                    val chaptersList = mutableListOf<Chapter>()
                    val chaptersData = contentDoc.get("chapters") as? List<Map<String, Any>> ?: emptyList()

                    for (chapterData in chaptersData) {
                        val number = (chapterData["number"] as? Long)?.toInt() ?: 0
                        val chapterTitle = chapterData["title"] as? String ?: ""
                        val content = chapterData["content"] as? String ?: ""

                        chaptersList.add(Chapter(number, chapterTitle, content))
                    }

                    // Create BookContent object
                    val bookContent = BookContent(
                        bookId = book.id,
                        title = contentDoc.getString("title") ?: book.title,
                        chapters = chaptersList
                    )

                    bookContentList.add(Pair(book, bookContent))
                }

                // Update UI after adding to list
                updateUI()
                if (bookContentList.size == 1) {
                    showLoading(false)
                }
            }
            .addOnFailureListener { e ->
                Timber.e("Error loading chapters for book ${book.id}: ${e.message}")

                // Add book with empty chapters
                val emptyBookContent = BookContent(book.id, book.title, emptyList())
                bookContentList.add(Pair(book, emptyBookContent))

                updateUI()
                showLoading(false)
            }
    }

    private fun filterBooks(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(bookContentList)
        } else {
            val searchQuery = query.lowercase()
            filteredList.addAll(bookContentList.filter { (book, _) ->
                book.title.lowercase().contains(searchQuery) ||
                        book.authorName.lowercase().contains(searchQuery) ||
                        book.category.lowercase().contains(searchQuery)
            })
        }

        bookContentAdapter.submitList(filteredList.toList())
        showEmptyState(filteredList.isEmpty())
    }

    private fun updateUI() {
        // Update the adapter with the data
        filteredList.clear()
        filteredList.addAll(bookContentList)
        bookContentAdapter.submitList(filteredList.toList())

        // Show empty state if needed
        showEmptyState(bookContentList.isEmpty())
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
        // First delete the book itself
        db.collection("Books").document(book.id)
            .delete()
            .addOnSuccessListener {
                // Then try to delete the book content
                db.collection("bookContents")
                    .whereEqualTo("bookId", book.id)
                    .get()
                    .addOnSuccessListener { contentDocs ->
                        for (doc in contentDocs) {
                            db.collection("bookContents").document(doc.id).delete()
                        }
                    }
                    .addOnFailureListener { e ->
                        Timber.e("Error deleting book content: ${e.message}")
                    }

                // Remove from our list and update UI
                val position = bookContentList.indexOfFirst { it.first.id == book.id }
                if (position != -1) {
                    bookContentList.removeAt(position)
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