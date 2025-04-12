package com.okta.senov.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.okta.senov.databinding.FragmentEditAuthorBinding
import timber.log.Timber
import java.util.UUID

class EditAuthorFragment : Fragment() {
    private var _binding: FragmentEditAuthorBinding? = null
    private val binding get() = _binding!!

    private var authorId: String? = null
    private var nameAuthorTitle: String? = null
    private var socialMediaTitle: String? = null
    private var bioAuthor: String? = null
    private var imageCover: String? = null

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
                binding.coverAuthorLabel.text = "Cover baru dipilih"
                binding.selectCoverButton.text = "Ganti gambar"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAuthorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments
        authorId = arguments?.getString("idAuthor")
        nameAuthorTitle = arguments?.getString("nameAuthor")
        socialMediaTitle = arguments?.getString("socialMedia")
        bioAuthor = arguments?.getString("bioAuthor")
        imageCover = arguments?.getString("imageUrl")

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.toolbarTitle.text = "Edit Author"
        binding.nameAuthorTitleEditText.setText(nameAuthorTitle)
        binding.socialMediaTitleEditText.setText(socialMediaTitle)
        binding.bioAuthorTitleEditText.setText(bioAuthor)
        binding.coverAuthorTitleEditText.setText(imageCover)

        // Menampilkan gambar cover yang ada dari URL
        if (imageCover?.isNotEmpty() == true) {
            Glide.with(requireContext())
                .load(imageCover)
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
        val newNameTitle = binding.nameAuthorTitleEditText.text.toString().trim()
        val newSocialMedia = binding.socialMediaTitleEditText.text.toString().trim()
        val newBioAuthor = binding.bioAuthorTitleEditText.text.toString().trim()
        val newImage = binding.coverAuthorTitleEditText.text.toString().trim()

        if (newNameTitle.isEmpty()) {
            binding.nameAuthorTitleEditText.error = "Name author cannot be empty"
            return
        }

        if (newSocialMedia.isEmpty()) {
            binding.socialMediaTitleEditText.error = "Social media author cannot be empty"
            return
        }
        if (newBioAuthor.isEmpty()) {
            binding.bioAuthorTitleEditText.error = "Biography author cannot be empty"
            return
        }
        if (newImage.isEmpty()) {
            binding.coverAuthorTitleEditText.error = "Cover author cannot be empty"
            return
        }

        showLoading(true)

        // Jika gambar diubah, upload gambar terlebih dahulu
        if (isImageChanged && imageUri != null) {
            uploadImage(newNameTitle, newSocialMedia, newBioAuthor, newImage)
        } else {
            // Jika gambar tidak diubah, gunakan URL yang ada
            updateAuthorData(
                newNameTitle,
                newSocialMedia,
                newBioAuthor,
                newImage
            )
        }
    }

    private fun uploadImage(nameAuthor: String, socialMediaAuthor: String, bioAuthor: String, imageAuthor: String) {
        try {
            showLoading(true)

            // Create a unique filename
            val fileName = "author_covers/${UUID.randomUUID()}.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

            imageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val uploadTask = storageRef.putBytes(bytes)

                    uploadTask.addOnProgressListener { taskSnapshot ->
                        val progress =
                            (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                        Timber.tag("EditBookFragment").d("Upload progress: $progress%")
                    }

                    uploadTask.addOnSuccessListener {
                        // Get the download URL once upload completes
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            // Now update the book with the new image URL
                            updateAuthorData(
                                nameAuthor,
                                socialMediaAuthor,
                                bioAuthor,
                                downloadUrl.toString()
                            )
                        }.addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(
                                context,
                                "Failed to get download URL: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Timber.tag("EditBookFragment").e(e, "Failed to get download URL: ${e.message}")
                        }
                    }.addOnFailureListener { e ->
                        showLoading(false)
                        Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
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

    private fun updateAuthorData(
        nameAuthor: String,
        socialMediaAuthor: String,
        bioAuthor: String,
        imageAuthor: String
    ) {
        // Update author in Firebase
        authorId?.let { id ->
            // Create updated author data
            val updatedAuthorData = hashMapOf(
                "nameAuthor" to nameAuthor,
                "socialMedia" to socialMediaAuthor,
                "bioAuthor" to bioAuthor,
                "imageUrl" to imageAuthor
            )

            // Update the document directly using the document ID
            db.collection("authors").document(id)
                .update(updatedAuthorData as Map<String, Any>)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(context, "Author updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } ?: run {
            showLoading(false)
            Toast.makeText(context, "Author ID is missing", Toast.LENGTH_SHORT).show()
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