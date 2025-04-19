package com.okta.senov.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentEditBookBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

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
    private val client = OkHttpClient()

    // Client ID Imgur
    private val IMGUR_CLIENT_ID = "79b90818f6bc407"

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
            // Tambahkan parameter waktu untuk memaksa refresh
            val imageUrlWithTimestamp = "$bookCover?t=${System.currentTimeMillis()}"
            Glide.with(requireContext())
                .load(imageUrlWithTimestamp)
                .skipMemoryCache(true)  // Skip cache memori
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Skip cache disk
                .placeholder(R.drawable.ic_book)
                .error(R.drawable.ic_error)
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
        val newImage = binding.coverBookTitleEditText.text.toString().trim()

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
        if (newImage.isEmpty() && !isImageChanged && (bookCover == null || bookCover!!.isEmpty())) {
            binding.coverBookTitleEditText.error = "Cover penulis tidak boleh kosong"
            Toast.makeText(context, "Cover buku tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Case 1: Image dipilih dari galeri - upload ke Imgur
        if (isImageChanged && imageUri != null) {
            uploadImageToImgur(newTitle, newAuthorBook, newCategoryBook, newDescriptionBook)
        }
        // Case 2: Tidak ada perubahan gambar, gunakan URL yang sudah ada
        else {
            // Gunakan URL gambar lama jika tidak ada perubahan gambar dan newImage kosong
            val imageUrl = if (newImage.isNotEmpty()) {
                newImage // Gunakan URL yang diketik manual jika ada
            } else {
                bookCover ?: "" // Gunakan URL lama jika tidak ada input manual
            }

            updateBookData(newTitle, newAuthorBook, newCategoryBook, newDescriptionBook, imageUrl)
        }
    }

    private fun uploadImageToImgur(
        nameTitle: String,
        nameAuthorBook: String,
        nameCategoryBook: String,
        descriptionBook: String
    ) {
        try {
            // Dapatkan bitmap dari Uri menggunakan ContentResolver
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)

            // Kompres bitmap ke base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64Image =
                android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)

            // Buat request ke Imgur API
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", base64Image)
                .build()

            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .header("Authorization", "Client-ID $IMGUR_CLIENT_ID")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // UI thread callback
                    activity?.runOnUiThread {
                        Timber.e(e, "Upload ke Imgur gagal")
                        showLoading(false)
                        Toast.makeText(
                            context,
                            "Upload gambar gagal: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Fallback ke URL yang ada
                        val fallbackUrl = binding.coverBookTitleEditText.text.toString().trim()
                        if (fallbackUrl.isNotEmpty()) {
                            updateBookData(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook, fallbackUrl)
                        } else if (bookCover?.isNotEmpty() == true) {
                            updateBookData(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook, bookCover!!)
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    // UI thread callback
                    activity?.runOnUiThread {
                        if (!response.isSuccessful || responseBody == null) {
                            Timber.e("Respons Imgur tidak berhasil: ${response.code}")
                            showLoading(false)
                            Toast.makeText(
                                context,
                                "Upload gagal: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Fallback ke URL yang ada
                            handleImgurUploadFailure(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook )
                            return@runOnUiThread
                        }

                        try {
                            // Parse response JSON
                            val jsonResponse = JSONObject(responseBody)
                            val success = jsonResponse.getBoolean("success")

                            if (success) {
                                val data = jsonResponse.getJSONObject("data")
                                val imgurUrl = data.getString("link")

                                Timber.d("Upload ke Imgur berhasil: $imgurUrl")

                                // Update URL di EditText untuk referensi
                                binding.coverBookTitleEditText.setText(imgurUrl)

                                // Update data buku dengan URL Imgur
                                updateBookData(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook, imgurUrl)
                            } else {
                                Timber.e("Upload ke Imgur gagal: ${jsonResponse.optString("status")}")
                                showLoading(false)
                                Toast.makeText(context, "Upload gambar gagal", Toast.LENGTH_SHORT)
                                    .show()

                                // Fallback ke URL yang ada
                                handleImgurUploadFailure(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook)
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error parsing JSON response")
                            showLoading(false)
                            Toast.makeText(
                                context,
                                "Error memproses respons: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Fallback ke URL yang ada
                            handleImgurUploadFailure(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Error dalam proses upload ke Imgur")
            showLoading(false)
            Toast.makeText(context, "Error memproses gambar: ${e.message}", Toast.LENGTH_SHORT)
                .show()

            // Fallback ke URL yang ada
            handleImgurUploadFailure(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook)
        }
    }
    private fun handleImgurUploadFailure(
        nameTitle: String,
        nameAuthorBook: String,
        nameCategoryBook: String,
        descriptionBook: String
    ) {
        // Coba gunakan URL yang dimasukkan manual jika ada
        val fallbackUrl = binding.coverBookTitleEditText.text.toString().trim()
        if (fallbackUrl.isNotEmpty()) {
            Timber.d("Menggunakan fallback URL dari EditText: $fallbackUrl")
            updateBookData(nameTitle, nameAuthorBook, nameCategoryBook,descriptionBook, fallbackUrl)
            Toast.makeText(
                context,
                "Menggunakan URL manual karena upload gagal",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Jika tidak ada, gunakan URL gambar yang lama jika ada
        else if (bookCover != null && bookCover!!.isNotEmpty()) {
            Timber.d("Menggunakan URL gambar lama: $bookCover")
            updateBookData(nameTitle, nameAuthorBook, nameCategoryBook, descriptionBook, bookCover!!)
            Toast.makeText(
                context,
                "Menggunakan URL gambar lama karena upload gagal",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Jika tidak ada opsi, tampilkan pesan error
        else {
            showLoading(false)
            Toast.makeText(context, "Upload gagal dan tidak ada URL alternatif", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun updateBookData(
        nameTitle: String,
        nameAuthorBook: String,
        nameCategoryBook: String,
        descriptionBook: String,
        imageBook: String
    ) {
        Timber.tag("EditBookFragment")
            .d("Memperbarui data penulis dengan URL gambar: $imageBook")

        // Tambahkan validasi
        if (bookId.isNullOrEmpty()) {
            showLoading(false)
            Toast.makeText(context, "ID book tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Update book in Firebase
        val updatedBookData = hashMapOf(
            "titleBook" to nameTitle,
            "nameAuthor" to nameAuthorBook,
            "nameCategory" to nameCategoryBook,
            "bookDescription" to descriptionBook,
            "fotoUrl" to imageBook
        )

        // Pastikan operasi update selesai dengan sukses
        db.collection("Books").document(bookId!!)
            .update(updatedBookData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                // Refresh gambar dengan URL terbaru
                Glide.with(requireContext())
                    .load(imageBook)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_book)
                    .error(R.drawable.ic_error)
                    .into(binding.coverImageView)

                Timber.tag("EditBookFragment").d("Data buku berhasil diperbarui")
                Toast.makeText(context, "Data buku berhasil diperbarui", Toast.LENGTH_SHORT)
                    .show()

                // Tambahkan delay singkat untuk memastikan data disimpan
                Handler(Looper.getMainLooper()).postDelayed({
                    // Set result OK dengan data tambahan
                    val intent = Intent()
                    intent.putExtra("UPDATED_AUTHOR_ID", bookId)
                    intent.putExtra("UPDATED_IMAGE_URL", imageBook)
                    requireActivity().setResult(Activity.RESULT_OK, intent)

                    // Tutup fragment
                    findNavController().popBackStack()
                }, 300) // Delay 300ms
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Timber.tag("EditBookFragment").e(e, "Gagal memperbarui data buku")
                Toast.makeText(
                    context,
                    "Gagal memperbarui buku: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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