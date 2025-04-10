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
import com.google.firebase.storage.FirebaseStorage
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAddAuthorBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

// Fragment untuk menambahkan data Author, termasuk mengunggah gambar ke Imgur dan menyimpannya ke Firestore.
class AddAuthorFragment : Fragment() {

    // Binding untuk view layout fragment_add_author.xml
    private var _binding: FragmentAddAuthorBinding? = null
    private val binding get() = _binding!!

    // Variabel untuk menyimpan URI gambar yang dipilih
    private var imageUri: Uri? = null

    // Inisialisasi Firestore dan OkHttpClient
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    private val storage = FirebaseStorage.getInstance().reference

    // Tag untuk logging
    private val TAG = "AddAuthorFragment"

    // Membuat dan menginisialisasi view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAuthorBinding.inflate(inflater, container, false)

        // Tombol untuk menyimpan data author
        binding.btnSimpanAuthor.setOnClickListener { saveAuthor() }

        // Tombol untuk memilih gambar dari galeri
        binding.btnPilihFoto.setOnClickListener { openGallery() }

        // Menangani tombol back
        setupBackButton()

        return binding.root
    }

    // Fungsi untuk membuka galeri dan memilih gambar
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Callback setelah user memilih gambar dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.ivFotoAuthor.setImageURI(imageUri)
            Toast.makeText(requireContext(), "Gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk validasi dan menyimpan data author
    private fun saveAuthor() {
        val idAuthor = binding.etAuthorId.text.toString().trim()
        val namaAuthor = binding.etNamaAuthor.text.toString().trim()
        val bioAuthor = binding.etBioAuthor.text.toString().trim()
        val socialMediaAuthor = binding.etSocialMediaAuthor.text.toString().trim()

        // Validasi input
        if (idAuthor.isEmpty() || namaAuthor.isEmpty() || bioAuthor.isEmpty() || socialMediaAuthor.isEmpty()) {
            Toast.makeText(requireContext(), "Data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        Timber.tag(TAG).d("Mulai menyimpan author dengan ID: $idAuthor")

        // Jika ada gambar yang dipilih, upload ke Imgur
        if (imageUri != null) {
            Timber.tag(TAG).d("Gambar dipilih, mengupload ke Imgur")
            uploadImageToImgur(idAuthor, namaAuthor, bioAuthor, socialMediaAuthor)
        } else {
            // Jika tidak ada gambar, langsung simpan ke Firestore
            Timber.tag(TAG).d("Tidak ada gambar yang dipilih, menyimpan tanpa gambar")
            saveAuthorToFirestore(idAuthor, namaAuthor, bioAuthor, socialMediaAuthor, null)
        }
    }

    // Fungsi untuk mengunggah gambar ke Imgur menggunakan API
    private fun uploadImageToImgur(
        idAuthor: String,
        nameAuthor: String,
        bioAuthor: String,
        socialMedia: String
    ) {
        try {
            // Ubah URI menjadi ByteArray
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val byteArray = stream.toByteArray()

            // Siapkan request body untuk multipart upload
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", "image.jpg",
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
                )
                .build()

            // Buat request ke Imgur
            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .header("Authorization", "Client-ID 79b90818f6bc407")
                .post(requestBody)
                .build()

            // Kirim request secara asynchronous
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Timber.tag(TAG).e("Error upload gambar ke Imgur: ${e.message}")
                    activity?.runOnUiThread {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Gagal mengunggah gambar ke Imgur: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            val jsonObject = JSONObject(responseBody)
                            val data = jsonObject.getJSONObject("data")
                            val imageUrl = data.getString("link")

                            // Lanjutkan simpan data author dengan imageUrl
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "URL gambar dari Imgur: $imageUrl", Toast.LENGTH_LONG).show()
                                saveAuthorToFirestore(idAuthor, nameAuthor, bioAuthor, socialMedia, imageUrl)
                            }
                        } catch (e: Exception) {
                            Timber.tag(TAG).e("Error parsing JSON dari Imgur: ${e.message}")
                            activity?.runOnUiThread {
                                showLoading(false)
                                Toast.makeText(requireContext(), "Gagal memproses respons dari Imgur: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Timber.tag(TAG).e("Gagal upload gambar ke Imgur: ${response.message}")
                        activity?.runOnUiThread {
                            showLoading(false)
                            Toast.makeText(requireContext(), "Gagal mengunggah gambar ke Imgur: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error umum saat upload gambar: ${e.message}")
            showLoading(false)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk menyimpan data author ke Firestore
    private fun saveAuthorToFirestore(
        idAuthor: String,
        nameAuthor: String,
        bioAuthor: String,
        socialMedia: String,
        imageUrl: String?
    ) {
        val authorData = hashMapOf(
            "idAuthor" to idAuthor,
            "nameAuthor" to nameAuthor,
            "bioAuthor" to bioAuthor,
            "socialMedia" to socialMedia,
            "imageUrl" to (imageUrl ?: "")
        )

        // Simpan dengan ID spesifik
        db.collection("authors")
            .document(idAuthor)
            .set(authorData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Data Author berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(requireContext(), "Gagal menambahkan data author: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Menampilkan dan menyembunyikan loading indicator pada tombol simpan
    private fun showLoading(isLoading: Boolean) {
        binding.btnSimpanAuthor.isEnabled = !isLoading
        binding.btnSimpanAuthor.text = if (isLoading) getString(R.string.saving) else getString(R.string.save)
    }

    // Fungsi untuk menghapus semua input setelah data berhasil disimpan
    private fun clearFields() {
        binding.etAuthorId.text?.clear()
        binding.etNamaAuthor.text?.clear()
        binding.etBioAuthor.text?.clear()
        binding.etSocialMediaAuthor.text?.clear()
        imageUri = null
        binding.ivFotoAuthor.setImageDrawable(null)
    }

    // Menangani aksi tombol back
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

    // Membersihkan binding ketika view dihancurkan
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1 // Kode request untuk memilih gambar
    }
}
