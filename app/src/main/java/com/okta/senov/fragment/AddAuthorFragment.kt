package com.okta.senov.fragment
//
//import android.app.Activity
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.okta.senov.databinding.FragmentAddAuthorBinding
//import java.io.IOException
//
//class AddAuthorFragment : Fragment() {
//    private var _binding: FragmentAddAuthorBinding? = null
//    private val binding get() = _binding!!
//
//    private var imageUri: Uri? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentAddAuthorBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.btnPilihFoto.setOnClickListener { openGallery() }
//        binding.btnSimpanAuthor.setOnClickListener { saveAuthor() }
//    }
//
//    private fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, PICK_IMAGE_REQUEST)
//    }
//
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
//            imageUri = data.data
//            try {
//                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
//                binding.ivFotoAuthor.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun saveAuthor() {
//        val namaAuthor = binding.etNamaAuthor.text.toString().trim()
//
//        if (namaAuthor.isEmpty() || imageUri == null) {
//            Toast.makeText(requireContext(), "Nama dan foto harus diisi!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        Toast.makeText(requireContext(), "Author $namaAuthor berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
//
//        findNavController().navigateUp()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    companion object {
//        private const val PICK_IMAGE_REQUEST = 1
//    }
//}


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.okta.senov.databinding.FragmentAddAuthorBinding
import java.util.UUID

class AddAuthorFragment : Fragment() {

    private var _binding: FragmentAddAuthorBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAuthorBinding.inflate(inflater, container, false)

        binding.btnPilihFoto.setOnClickListener { openGallery() }
        binding.btnSimpanAuthor.setOnClickListener { saveAuthor() }

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
        val namaAuthor = binding.etNamaAuthor.text.toString().trim()
        val bioAuthor = binding.etBioAuthor.text.toString().trim()
        val socialMediaAuthor = binding.etSocialMediaAuthor.text.toString().trim()

        if (namaAuthor.isEmpty() || bioAuthor.isEmpty()) {
            Toast.makeText(requireContext(), "Nama, email, dan bio harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadImageAndSaveData(namaAuthor, bioAuthor, socialMediaAuthor)
        } else {
            saveAuthorToFirestore(namaAuthor, bioAuthor, socialMediaAuthor, null)
        }
    }

    private fun uploadImageAndSaveData(nama: String, bio: String, socialMedia: String) {
        val fileName = "authors/${UUID.randomUUID()}.jpg"
        val imageRef = storage.child(fileName)

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveAuthorToFirestore(nama, bio, socialMedia, downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mengunggah gambar!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveAuthorToFirestore(nama: String, bio: String, socialMedia: String?, fotoUrl: String?) {
        val authorData = hashMapOf(
            "nama" to nama,
            "bio" to bio,
            "socialMedia" to (socialMedia ?: ""),
            "fotoUrl" to (fotoUrl ?: "")
        )

        db.collection("authors")
            .add(authorData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Author berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menambahkan author!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        binding.etNamaAuthor.text.clear()
        binding.etBioAuthor.text.clear()
        binding.etSocialMediaAuthor.text.clear()
        imageUri = null
        binding.ivFotoAuthor.setImageDrawable(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
