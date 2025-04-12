package com.okta.senov.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.okta.senov.R
import com.okta.senov.databinding.FragmentEditBookBinding
import timber.log.Timber
import java.util.UUID

class EditBookFragment : Fragment() {
    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!

    private var bookId: String? = null
    private var bookTitle: String? = null
    private var bookAuthor: String? = null
    private var bookCategory: String? = null
    private var bookDescription: String? = null
    private var bookCover: String? = null

    private var imageUri: Uri? = null
    private var isImageChanged = false

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                isImageChanged = true
                // Menampilkan gambar yang dipilih
                Glide.with(requireContext())
                    .load(uri)
                    .into(binding.coverImageView)

                // Memperbarui UI untuk menunjukkan gambar baru dipilih
                binding.coverBookLabel.text = "Cover baru dipilih"
                binding.selectCoverButton.text = "Ganti gambar"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments
        bookId = arguments?.getString("idBook")
        bookTitle = arguments?.getString("titleBook")
        bookAuthor = arguments?.getString("nameAuthor")
        bookCategory = arguments?.getString("nameCategory")
        bookDescription = arguments?.getString("bookDescription")
        bookCover = arguments?.getString("fotoUrl") ?: ""

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.toolbarTitle.text = "Edit Book"
        binding.bookTitleEditText.setText(bookTitle)
        binding.authorBookTitleEditText.setText(bookAuthor)
        binding.categoryBookTitleEditText.setText(bookCategory)
        binding.descriptionBookTitleEditText.setText(bookDescription)

        // Menampilkan gambar cover yang ada dari URL
        if (bookCover?.isNotEmpty() == true) {
            Glide.with(requireContext())
                .load(bookCover)
                .placeholder(R.drawable.ic_profile_placeholder) // Ganti dengan placeholder yang sesuai
                .error(R.drawable.ic_error) // Ganti dengan gambar error yang sesuai
                .into(binding.coverImageView)
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.saveButton.setOnClickListener {
            saveBookChanges()
        }

        binding.selectCoverButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveBookChanges() {
        val newTitle = binding.bookTitleEditText.text.toString().trim()
        val newAuthorBook = binding.authorBookTitleEditText.text.toString().trim()
        val newCategoryBook = binding.categoryBookTitleEditText.text.toString().trim()
        val newDescriptionBook = binding.descriptionBookTitleEditText.text.toString().trim()

        if (newTitle.isEmpty()) {
            binding.bookTitleEditText.error = "Title cannot be empty"
            return
        }

        if (newAuthorBook.isEmpty()) {
            binding.authorBookTitleEditText.error = "Author book cannot be empty"
            return
        }
        if (newCategoryBook.isEmpty()) {
            binding.categoryBookTitleEditText.error = "Category book cannot be empty"
            return
        }
        if (newDescriptionBook.isEmpty()) {
            binding.descriptionBookTitleEditText.error = "Description book cannot be empty"
            return
        }
        if (bookCover?.isEmpty() == true && imageUri == null) {
            Toast.makeText(context, "Please select a cover image", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Jika gambar diubah, upload gambar terlebih dahulu
        if (isImageChanged && imageUri != null) {
            uploadImage(newTitle, newAuthorBook, newCategoryBook, newDescriptionBook)
        } else {
            // Jika gambar tidak diubah, gunakan URL yang ada
            updateBookData(
                newTitle,
                newAuthorBook,
                newCategoryBook,
                newDescriptionBook,
                bookCover ?: ""
            )
        }
    }

    private fun uploadImage(title: String, author: String, category: String, description: String) {
        try {
            showLoading(true)

            // Create a unique filename
            val fileName = "book_covers/${UUID.randomUUID()}.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

            imageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val uploadTask = storageRef.putBytes(bytes)

                    uploadTask.addOnProgressListener { taskSnapshot ->
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                        Timber.tag("EditBookFragment").d("Upload progress: $progress%")
                    }

                    uploadTask.addOnSuccessListener {
                        // Get the download URL once upload completes
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            // Now update the book with the new image URL
                            updateBookData(title, author, category, description, downloadUrl.toString())
                        }.addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            Timber.tag("EditBookFragment").e(e, "Failed to get download URL: ${e.message}")
                        }
                    }.addOnFailureListener { e ->
                        showLoading(false)
                        Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        Timber.tag("EditBookFragment").e(e, "Failed to upload image: ${e.message}")
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                showLoading(false)
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Timber.tag("EditBookFragment").e(e, "Error during upload process: ${e.message}")
        }
    }
    private fun updateBookData(
        title: String,
        author: String,
        category: String,
        description: String,
        coverUrl: String
    ) {
        // Update book in Firebase
        bookId?.let { id ->
            // Create updated book data
            val updatedBookData = hashMapOf(
                "titleBook" to title,
                "nameAuthor" to author,
                "nameCategory" to category,
                "bookDescription" to description,
                "fotoUrl" to coverUrl
            )

            // Update the document directly using the document ID
            db.collection("Books").document(id)
                .update(updatedBookData as Map<String, Any>)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(context, "Book updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } ?: run {
            showLoading(false)
            Toast.makeText(context, "Book ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}