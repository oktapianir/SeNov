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
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddAuthorFragment : Fragment() {

    private var _binding: FragmentAddAuthorBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAuthorBinding.inflate(inflater, container, false)

        binding.etAuthorId.setOnClickListener { saveAuthor() }
        binding.etNamaAuthor.setOnClickListener { saveAuthor() }
        binding.etSocialMediaAuthor.setOnClickListener { saveAuthor() }
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

        // Show loading indicator
        showLoading(true)

        if (imageUri != null) {
            uploadImageToImgur(
                idAuthor,
                namaAuthor,
                bioAuthor,
                socialMediaAuthor
            )
        } else {
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
                            // Use the actual Imgur URL here
                            saveAuthorToFirestore(
                                idAuthor,
                                nameAuthor,
                                bioAuthor,
                                socialMedia,
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
        val authorData = hashMapOf(
            "idAuthor" to idAuthor,
            "nameAuthor" to nameAuthor,
            "bioAuthor" to bioAuthor,
            "socialMedia" to socialMedia,
            "image" to (imageUrl ?: "")
        )

        db.collection("authors")
            .add(authorData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    "Data Author berhasil ditambahkan!",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Gagal menambahkan data author!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        // Anda perlu menambahkan ProgressBar di layout Anda
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpanAuthor.isEnabled = !isLoading
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