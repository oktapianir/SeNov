package com.okta.senov.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.databinding.FragmentAddContentBookBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import com.okta.senov.model.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class AddContentBookFragment : Fragment() {
    private var _binding: FragmentAddContentBookBinding? = null
    private val binding get() = _binding!!
    //Firebase Instance
    private val db = FirebaseFirestore.getInstance()
    //list for storing books
    private val booksList = mutableListOf<Book>()
    //list for book display in dropdown
    private val bookDisplayList = mutableListOf<String>()
    //adapter for dropdown
    private lateinit var bookAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContentBookBinding.inflate(inflater, container, false)
        //setup back button functionality
        setupBackButton()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initialize the book dropdown
        setupBookDropdown()

        binding.actvBookId.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT

        //handle add chapter button click
        binding.btnAddChapter.setOnClickListener {
            val bookId = extractBookId(binding.actvBookId.text.toString().trim())
            val bookContentId = binding.etBookContentId.text.toString().trim()
            val chapterNumber = binding.etChapterNumber.text.toString().trim().toIntOrNull()
            val chapterTitle = binding.etChapterTitle.text.toString().trim()
            val chapterContent = binding.etChapterContent.text.toString().trim()

            //validate input before adding chapter
            if (bookId.isNotEmpty() && bookContentId.isNotEmpty() && chapterNumber != null && chapterTitle.isNotEmpty() && chapterContent.isNotEmpty()) {
                val newChapter = Chapter(
                    number = chapterNumber,
                    title = chapterTitle,
                    content = chapterContent
                )

                //add chapter in a coroutine (background task)
                CoroutineScope(Dispatchers.IO).launch {
                    addChapterToBook(bookId, newChapter)
                }
            } else {
                Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.actvBookId.setOnClickListener {
            binding.actvBookId.showDropDown()
        }
    }

    private fun setupBookDropdown() {
        // Use Android's built-in layout for dropdown items
        bookAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            bookDisplayList
        )

        val bookIdDropdown = binding.actvBookId
        bookIdDropdown.setAdapter(bookAdapter)

        //make dropdown non-editable
        bookIdDropdown.inputType = InputType.TYPE_NULL

        // Show dropdown when clicked
        bookIdDropdown.setOnClickListener {
            bookIdDropdown.showDropDown()
        }

        // Handle selection of book from dropdown
        bookIdDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedBookDisplay = bookDisplayList[position]
            bookIdDropdown.setText(selectedBookDisplay, false)
        }

//        // For debugging, add some test items directly first
//        bookDisplayList.add("TEST - Book Title 1")
//        bookDisplayList.add("TEST - Book Title 2")
//        bookAdapter.notifyDataSetChanged()

        // Then fetch the real books firestore
        fetchBooks()
    }

    private fun fetchBooks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //fetch books from firestore
                val snapshot = db.collection("Books").get().await()
                Timber.tag("AddContentBook").d("Books fetched: ${snapshot.size()}")

                // Get raw document data for debugging
                for (document in snapshot.documents) {
                    Timber.tag("AddContentBook").d("Raw doc ID: ${document.id}, data: ${document.data}")
                }

                val newBooksList = mutableListOf<Book>()
                val newDisplayList = mutableListOf<String>()

                // Iterate over documents to create Book objects
                for (document in snapshot.documents) {
                    val bookId = document.id
                    val book = document.toObject(Book::class.java)
                    val title = document.getString("titleBook") ?: "Untitled Book"

                    if (book != null) {
                        val updatedBook = book.copy(id = bookId)
                        newBooksList.add(updatedBook)
                        newDisplayList.add("$bookId - $title") // Pastikan formatnya benar
                        Timber.tag("AddContentBook").d("Adding book: $bookId - $title")
                    }
                }

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    booksList.clear()
                    booksList.addAll(newBooksList)

                    bookDisplayList.clear()
                    bookDisplayList.addAll(newDisplayList)

                    // Create a new adapter with custom layout
                    bookAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        android.R.id.text1,  // Add this reference to the TextView ID
                        bookDisplayList
                    )

                    binding.actvBookId.setAdapter(bookAdapter)

                    // Notify adapter about data changes
                    bookAdapter.notifyDataSetChanged()

//                    Toast.makeText(
//                        requireContext(),
//                        "Loaded ${bookDisplayList.size} books",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            } catch (e: Exception) {
                Timber.tag("AddContentBook").e(e, "Error loading books")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error loading books: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    // Extract the book ID from the dropdown display text
    private fun extractBookId(displayText: String): String {
        return if (displayText.contains(" - ")) {
            displayText.split(" - ")[0].trim()
        } else {
            displayText
        }
    }

    // Function to add a chapter to the selected book
    private suspend fun addChapterToBook(bookId: String, chapter: Chapter) {
        try {
            val docRef = db.collection("bookContents").document(bookId)
            val bookSnapshot = docRef.get().await()

            // If book content exists, update chapters
            if (bookSnapshot.exists()) {
                val bookContent = bookSnapshot.toObject(BookContent::class.java)
                val updatedChapters = bookContent?.chapters?.toMutableList() ?: mutableListOf()
                updatedChapters.add(chapter)

                docRef.update("chapters", updatedChapters).await()
            } else {
                // Create a new BookContent document if it doesn't exist
                val bookTitle = booksList.find { it.id == bookId }?.title ?: "Judul Tidak Diketahui"

                val newBookContent = BookContent(
                    bookId = bookId,
                    title = bookTitle,
                    chapters = listOf(chapter)
                )
                docRef.set(newBookContent).await()
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Konten berhasil ditambahkan!", Toast.LENGTH_SHORT)
                    .show()
                clearFields()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Gagal menambahkan konten: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Clear all input fields
    private fun clearFields() {
        binding.actvBookId.text?.clear()
        binding.etBookContentId.text?.clear()
        binding.etChapterNumber.text?.clear()
        binding.etChapterTitle.text?.clear()
        binding.etChapterContent.text?.clear()
    }

    // Handle the back button press to navigate back
    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}