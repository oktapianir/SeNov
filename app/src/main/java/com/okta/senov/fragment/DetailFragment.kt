package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentDetailBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import timber.log.Timber

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DetailFragmentArgs by navArgs()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val book = args.bookArg
        setupBookDetails(book)
    }

    private fun setupBookDetails(book: Book) {
        binding.apply {
            bookTitle.text = book.title
            authorName.text = book.authorName
            bookDescriptionTextView.text = book.description

            Timber.tag("ImageLoading").d("Mencoba memuat gambar dari URL: ${book.image}")

            Glide.with(bookCoverImageView.context)
                .load(book.image)
                .error(R.drawable.ic_error)
                .into(bookCoverImageView)
        }

        // Log the book details
        Timber.tag("BookDetails")
            .d("Book: ID=${book.id}, Title=${book.title}, Author=${book.authorName}, Description=${book.description}")

        // Only fetch from Firestore if description is empty
        if (book.description.isBlank()) {
            fetchBookDetails(book.id)
        }

        setupBackButton()
    }

    private fun fetchBookDetails(bookId: String) {
        // Add logging to check the bookId value
        Timber.tag("Firestore").d("Attempting to fetch book with ID: $bookId")

        db.collection("Books").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Log the entire document for debugging
                    Timber.tag("Firestore").d("Document data: ${document.data}")

                    val author = document.getString("nameAuthor") ?: "Tidak ada data"
                    val description =
                        document.getString("bookDescription") ?: "Deskripsi tidak tersedia"
                    val imageUrl = document.getString("imageUrl")

                    binding.authorName.text = author
                    binding.bookDescriptionTextView.text = description

                    if (!imageUrl.isNullOrEmpty()) {
                        Timber.tag("ImageLoading").d("Memuat gambar dari Firestore URL: $imageUrl")

                        Glide.with(binding.bookCoverImageView.context)
                            .load(imageUrl)
                            .error(R.drawable.ic_error)
                            .into(binding.bookCoverImageView)
                    }
                    Timber.tag("Firestore").d("Data successfully retrieved: $author - $description")
                } else {
                    Timber.tag("Firestore").e("Document not found")
                    binding.authorName.visibility = View.GONE
                    binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
                }
            }
            .addOnFailureListener { e ->
                Timber.tag("FirestoreError").e("Failed to retrieve data: ${e.message}")
                binding.authorName.visibility = View.GONE
                binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
            }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        val book = args.bookArg
        binding.listenButton.setOnClickListener {
            val bookContent = BookContent(
                bookId = book.id,
                title = book.title,
                chapters = emptyList()
            )
            val action = DetailFragmentDirections.actionDetailToBookreader(bookContent)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
