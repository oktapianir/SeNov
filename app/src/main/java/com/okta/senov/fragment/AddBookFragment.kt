package com.okta.senov.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAddBookBinding
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class AddBookFragment : Fragment(R.layout.fragment_add_book) {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!

    // URI gambar yang dipilih
    private var imageUri: Uri? = null

    // Instance Firebase dan OkHttp client
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.tag("AddBookFragment").d("onCreateView called")

        _binding = FragmentAddBookBinding.inflate(inflater, container, false)

        // Set click listener pada elemen input dan tombol
        binding.btnPilihCover.setOnClickListener { openGallery() }
        binding.btnSimpanBook.setOnClickListener { saveBook() }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.bookListFragment)
        }

        // Tombol kembali
        setupBackButton()

        return binding.root
    }

    // Buka galeri untuk memilih gambar
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Mendapatkan hasil dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.ivBookCover.setImageURI(imageUri)
        }
    }

    // Validasi data dan proses penyimpanan
    private fun saveBook() {
        val idBook = binding.etBookId.text.toString().trim()
        val titleBook = binding.etBookTitle.text.toString().trim()
        val nameAuthor = binding.etNameAuthor.text.toString().trim()
        val bookDescription = binding.etBookDescription.text.toString().trim()
        val nameCategory = binding.etNameCategory.text.toString().trim()

        // Validasi input wajib
        if (idBook.isEmpty() || titleBook.isEmpty() || nameAuthor.isEmpty() || bookDescription.isEmpty() || nameCategory.isEmpty()) {
            Toast.makeText(requireContext(), "Data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika ada gambar, upload ke Imgur dulu
        if (imageUri != null) {
            uploadImageToImgur(idBook, titleBook, nameAuthor, bookDescription, nameCategory)
        } else {
            // Simpan langsung ke Firestore jika tanpa gambar
            saveBookToFirestore(idBook, titleBook, nameAuthor, bookDescription, nameCategory, null)
        }
    }

    // Upload gambar ke Imgur
    private fun uploadImageToImgur(
        idBook: String,
        titleBook: String,
        nameAuthor: String,
        bookDescription: String,
        nameCategory: String
    ) {
        try {
            // Ubah gambar menjadi byte array
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val byteArray = stream.toByteArray()

            // Buat request multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", "image.jpg",
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
                )
                .build()

            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .header("Authorization", "Client-ID 79b90818f6bc407")
                .post(requestBody)
                .build()

            // Kirim request secara asynchronous
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengunggah gambar ke Imgur: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject(responseBody)
                        val data = jsonObject.getJSONObject("data")
                        val imageUrl = data.getString("link")

                        activity?.runOnUiThread {
                            saveBookToFirestore(
                                idBook,
                                titleBook,
                                nameAuthor,
                                bookDescription,
                                nameCategory,
                                imageUrl
                            )
                        }
                    } else {
                        activity?.runOnUiThread {
                            showLoading(false)
                            Toast.makeText(
                                requireContext(),
                                "Gagal mengunggah gambar ke Imgur: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Simpan data buku ke Firestore
    private fun saveBookToFirestore(
        idBook: String,
        titleBook: String,
        nameAuthor: String,
        bookDescription: String,
//        bookContent: String,
        nameCategory: String,
        fotoUrl: String?
    ) {
        val bookData = hashMapOf(
            "idBook" to idBook,
            "titleBook" to titleBook,
            "nameAuthor" to nameAuthor,
            "bookDescription" to bookDescription,
            "nameCategory" to nameCategory,
            "fotoUrl" to (fotoUrl ?: "")
        )

        db.collection("Books")
            .document(idBook)
            .set(bookData)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Data Buku berhasil ditambahkan!",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menambahkan data buku!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // Menampilkan loading state
    private fun showLoading(isLoading: Boolean) {
        // Implementasi jika ingin menambahkan ProgressBar
        binding.btnSimpanBook.isEnabled = !isLoading
    }

    // Reset semua input
    private fun clearFields() {
        binding.etBookId.text?.clear()
        binding.etBookTitle.text?.clear()
        binding.etNameAuthor.text?.clear()
        binding.etBookDescription.text?.clear()
        binding.etNameCategory.text?.clear()
        imageUri = null
        binding.ivBookCover.setImageDrawable(null)
    }

    // Tombol kembali menggunakan NavController
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
        Timber.tag("AddBookFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
