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
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentEditAuthorBinding
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

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

        // Memastikan Firebase terinisialisasi dengan benar
        try {
            FirebaseApp.initializeApp(requireContext())
        } catch (e: Exception) {
            Timber.e(e, "Firebase sudah terinisialisasi atau gagal inisialisasi")
        }

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
            // Tambahkan parameter waktu untuk memaksa refresh
            val imageUrlWithTimestamp = "$imageCover?t=${System.currentTimeMillis()}"
            Glide.with(requireContext())
                .load(imageUrlWithTimestamp)
                .skipMemoryCache(true)  // Skip cache memori
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Skip cache disk
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_error)
                .into(binding.coverImageView)
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.saveButton.setOnClickListener {
            saveAuthorChanges()
        }

        binding.selectCoverButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveAuthorChanges() {
        val newNameTitle = binding.nameAuthorTitleEditText.text.toString().trim()
        val newSocialMedia = binding.socialMediaTitleEditText.text.toString().trim()
        val newBioAuthor = binding.bioAuthorTitleEditText.text.toString().trim()
        val newImageUrl = binding.coverAuthorTitleEditText.text.toString().trim()

        // Validasi input
        if (newNameTitle.isEmpty()) {
            binding.nameAuthorTitleEditText.error = "Nama penulis tidak boleh kosong"
            return
        }

        if (newSocialMedia.isEmpty()) {
            binding.socialMediaTitleEditText.error = "Media sosial penulis tidak boleh kosong"
            return
        }

        if (newBioAuthor.isEmpty()) {
            binding.bioAuthorTitleEditText.error = "Biografi penulis tidak boleh kosong"
            return
        }

        // Jika gambar tidak dipilih dari galeri, URL gambar harus diisi
        if (newImageUrl.isEmpty() && !isImageChanged) {
            binding.coverAuthorTitleEditText.error = "Cover penulis tidak boleh kosong"
            return
        }

        showLoading(true)

        // Case 1: Image dipilih dari galeri - upload ke Imgur
        if (isImageChanged && imageUri != null) {
            uploadImageToImgur(newNameTitle, newSocialMedia, newBioAuthor)
        }
        // Case 2: URL image diubah manual di EditText atau tidak ada perubahan
        else {
            // Gunakan URL yang ada di EditText (bisa URL baru atau URL lama)
            updateAuthorData(newNameTitle, newSocialMedia, newBioAuthor, newImageUrl)
        }
    }

    private fun uploadImageToImgur(
        nameAuthor: String,
        socialMediaAuthor: String,
        bioAuthor: String
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
                        val fallbackUrl = binding.coverAuthorTitleEditText.text.toString().trim()
                        if (fallbackUrl.isNotEmpty()) {
                            updateAuthorData(nameAuthor, socialMediaAuthor, bioAuthor, fallbackUrl)
                        } else if (imageCover?.isNotEmpty() == true) {
                            updateAuthorData(nameAuthor, socialMediaAuthor, bioAuthor, imageCover!!)
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
                            handleImgurUploadFailure(nameAuthor, socialMediaAuthor, bioAuthor)
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
                                binding.coverAuthorTitleEditText.setText(imgurUrl)

                                // Update data penulis dengan URL Imgur
                                updateAuthorData(nameAuthor, socialMediaAuthor, bioAuthor, imgurUrl)
                            } else {
                                Timber.e("Upload ke Imgur gagal: ${jsonResponse.optString("status")}")
                                showLoading(false)
                                Toast.makeText(context, "Upload gambar gagal", Toast.LENGTH_SHORT)
                                    .show()

                                // Fallback ke URL yang ada
                                handleImgurUploadFailure(nameAuthor, socialMediaAuthor, bioAuthor)
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
                            handleImgurUploadFailure(nameAuthor, socialMediaAuthor, bioAuthor)
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
            handleImgurUploadFailure(nameAuthor, socialMediaAuthor, bioAuthor)
        }
    }

    private fun handleImgurUploadFailure(
        nameAuthor: String,
        socialMediaAuthor: String,
        bioAuthor: String
    ) {
        // Coba gunakan URL yang dimasukkan manual jika ada
        val fallbackUrl = binding.coverAuthorTitleEditText.text.toString().trim()
        if (fallbackUrl.isNotEmpty()) {
            Timber.d("Menggunakan fallback URL dari EditText: $fallbackUrl")
            updateAuthorData(nameAuthor, socialMediaAuthor, bioAuthor, fallbackUrl)
            Toast.makeText(
                context,
                "Menggunakan URL manual karena upload gagal",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Jika tidak ada, gunakan URL gambar yang lama jika ada
        else if (imageCover != null && imageCover!!.isNotEmpty()) {
            Timber.d("Menggunakan URL gambar lama: $imageCover")
            updateAuthorData(nameAuthor, socialMediaAuthor, bioAuthor, imageCover!!)
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

    private fun updateAuthorData(
        nameAuthor: String,
        socialMediaAuthor: String,
        bioAuthor: String,
        imageAuthor: String
    ) {
        Timber.tag("EditAuthorFragment")
            .d("Memperbarui data penulis dengan URL gambar: $imageAuthor")

        // Tambahkan validasi
        if (authorId.isNullOrEmpty()) {
            showLoading(false)
            Toast.makeText(context, "ID penulis tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Update author in Firebase
        val updatedAuthorData = hashMapOf(
            "nameAuthor" to nameAuthor,
            "socialMedia" to socialMediaAuthor,
            "bioAuthor" to bioAuthor,
            "imageUrl" to imageAuthor
        )

        // Pastikan operasi update selesai dengan sukses
        db.collection("authors").document(authorId!!)
            .update(updatedAuthorData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                // Refresh gambar dengan URL terbaru
                Glide.with(requireContext())
                    .load(imageAuthor)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_error)
                    .into(binding.coverImageView)

                Timber.tag("EditAuthorFragment").d("Data penulis berhasil diperbarui")
                Toast.makeText(context, "Data penulis berhasil diperbarui", Toast.LENGTH_SHORT)
                    .show()

                // Tambahkan delay singkat untuk memastikan data disimpan
                Handler(Looper.getMainLooper()).postDelayed({
                    // Set result OK dengan data tambahan
                    val intent = Intent()
                    intent.putExtra("UPDATED_AUTHOR_ID", authorId)
                    intent.putExtra("UPDATED_IMAGE_URL", imageAuthor)
                    requireActivity().setResult(Activity.RESULT_OK, intent)

                    // Tutup fragment
                    findNavController().popBackStack()
                }, 300) // Delay 300ms
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Timber.tag("EditAuthorFragment").e(e, "Gagal memperbarui data penulis")
                Toast.makeText(
                    context,
                    "Gagal memperbarui penulis: ${e.message}",
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