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

class AddAuthorFragment : Fragment() {

    private var _binding: FragmentAddAuthorBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    private val storage = FirebaseStorage.getInstance().reference
    private val TAG = "AddAuthorFragment" // Tag untuk logging

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAuthorBinding.inflate(inflater, container, false)

        binding.btnSimpanAuthor.setOnClickListener { saveAuthor() }
        binding.btnPilihFoto.setOnClickListener { openGallery() }
        setupBackButton()
        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.ivFotoAuthor.setImageURI(imageUri)
            Toast.makeText(requireContext(), "Gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAuthor() {
        val idAuthor = binding.etAuthorId.text.toString().trim()
        val namaAuthor = binding.etNamaAuthor.text.toString().trim()
        val bioAuthor = binding.etBioAuthor.text.toString().trim()
        val socialMediaAuthor = binding.etSocialMediaAuthor.text.toString().trim()

        if (idAuthor.isEmpty() || namaAuthor.isEmpty() || bioAuthor.isEmpty() || socialMediaAuthor.isEmpty()) {
            Toast.makeText(requireContext(), "Data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading indicator
        showLoading(true)

        Timber.tag(TAG).d("Mulai menyimpan author dengan ID: $idAuthor")

        if (imageUri != null) {
            Timber.tag(TAG).d("Gambar dipilih, mengupload ke Imgur")
            uploadImageToImgur(
                idAuthor,
                namaAuthor,
                bioAuthor,
                socialMediaAuthor
            )
        } else {
            Timber.tag(TAG).d("Tidak ada gambar yang dipilih, menyimpan tanpa gambar")
            saveAuthorToFirestore(
                idAuthor,
                namaAuthor,
                bioAuthor,
                socialMediaAuthor,
                null
            )
        }
    }

    private fun uploadImageToImgur(
        idAuthor: String,
        nameAuthor: String,
        bioAuthor: String,
        socialMedia: String
    ) {
        try {
            Timber.tag(TAG).d("Memulai upload gambar ke Imgur")
            // Konversi Uri ke ByteArray
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val byteArray = stream.toByteArray()

            // Buat request body dengan gambar
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
                )
                .build()

            // Buat request untuk Imgur API
            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .header("Authorization", "Client-ID 79b90818f6bc407")
                .post(requestBody)
                .build()

            Timber.tag(TAG).d("Mengirim request ke Imgur API")
            // Kirim request secara asynchronous
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Timber.tag(TAG).e("Error upload gambar ke Imgur: ${e.message}")
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
                    Timber.tag(TAG)
                        .d("Mendapat respons dari Imgur API, status code: ${response.code}")

                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            Timber.tag(TAG).d("Respon dari Imgur: $responseBody")

                            val jsonObject = JSONObject(responseBody)
                            val data = jsonObject.getJSONObject("data")
                            val imageUrl = data.getString("link")

                            Timber.tag(TAG).d("Berhasil mendapatkan URL gambar: $imageUrl")

                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "URL gambar dari Imgur: $imageUrl", Toast.LENGTH_LONG).show()

                                // Gunakan URL gambar dari Imgur
                                saveAuthorToFirestore(
                                    idAuthor,
                                    nameAuthor,
                                    bioAuthor,
                                    socialMedia,
                                    imageUrl
                                )
                            }
                        } catch (e: Exception) {
                            Timber.tag(TAG).e("Error parsing JSON dari Imgur: ${e.message}")
                            activity?.runOnUiThread {
                                showLoading(false)
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal memproses respons dari Imgur: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Timber.tag(TAG).e("Gagal upload gambar ke Imgur: ${response.message}")
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
            Timber.tag(TAG).e("Error umum saat upload gambar: ${e.message}")
            showLoading(false)
            Toast.makeText(
                requireContext(),
                "Error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveAuthorToFirestore(
        idAuthor: String,
        nameAuthor: String,
        bioAuthor: String,
        socialMedia: String,
        imageUrl: String?
    ) {
        Timber.tag(TAG).d("Menyimpan author ke Firestore dengan imageUrl: $imageUrl")

        val authorData = hashMapOf(
            "idAuthor" to idAuthor,
            "nameAuthor" to nameAuthor,
            "bioAuthor" to bioAuthor,
            "socialMedia" to socialMedia,
            "imageUrl" to (imageUrl ?: "") // Mengubah nama field dari "image" ke "imageUrl"
        )

        Timber.tag(TAG).d("Data author yang akan disimpan: $authorData")

        // Menggunakan ID spesifik alih-alih random ID
        db.collection("authors")
            .document(idAuthor)
            .set(authorData)
            .addOnSuccessListener {
                Timber.tag(TAG).d("Author berhasil disimpan ke Firestore")
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    "Data Author berhasil ditambahkan!",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Timber.tag(TAG).e("Error menyimpan author ke Firestore: ${e.message}")
                showLoading(false)
                Toast.makeText(requireContext(), "Gagal menambahkan data author: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSimpanAuthor.isEnabled = !isLoading
        if (isLoading) {
            binding.btnSimpanAuthor.text = getString(R.string.saving)
        } else {
            binding.btnSimpanAuthor.text = getString(R.string.save)
        }
    }

    private fun clearFields() {
        binding.etAuthorId.text?.clear()
        binding.etNamaAuthor.text?.clear()
        binding.etBioAuthor.text?.clear()
        binding.etSocialMediaAuthor.text?.clear()
        imageUri = null
        binding.ivFotoAuthor.setImageDrawable(null)
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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}