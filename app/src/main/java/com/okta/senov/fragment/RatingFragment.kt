package com.okta.senov.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.databinding.FragmentRatingBinding
import com.okta.senov.R
import timber.log.Timber


class RatingFragment : Fragment() {

    private lateinit var binding: FragmentRatingBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var bookId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Ambil bookId dari argument fragment
        val args = RatingFragmentArgs.fromBundle(requireArguments())
        bookId = args.BOOKID
        Timber.tag("RatingFragment").d("Received bookId: $bookId")


        // Ambil data buku untuk ditampilkan
        if (bookId.isNotEmpty()) {
            loadBookData(bookId)
        }

        // Set listener untuk rating bar
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.tvRatingValue.text = "${rating}/5.0"
        }

        // Set listener untuk tombol submit
        binding.btnSubmitRating.setOnClickListener {
            submitRating()
        }
        setupBackButton()
    }

    private fun loadBookData(bookId: String) {
        firestore.collection("Books").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Isi data buku ke UI
                    binding.tvBookTitle.text = document.getString("titleBook") ?: "Judul Buku"
                    binding.tvAuthor.text = document.getString("nameAuthor") ?: "Penulis"

                    // Load gambar sampul buku menggunakan Glide jika ada URL gambar
                    document.getString("fotoUrl")?.let { coverUrl ->
                        if (coverUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(coverUrl)
                                .placeholder(R.drawable.ic_book)
                                .into(binding.ivBookCover)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitRating() {
        // Validasi user sudah login
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Silahkan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi bookId ada
        if (bookId.isEmpty()) {
            Toast.makeText(requireContext(), "Data buku tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi rating
        val rating = binding.ratingBar.rating
        if (rating <= 0) {
            Toast.makeText(requireContext(), "Silahkan berikan rating terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Persiapkan data rating
        val userEmail = currentUser.email ?: ""
        val review = binding.etReview.text.toString().trim()
        val isRecommended = binding.cbRecommend.isChecked
        val timestamp = FieldValue.serverTimestamp()

        // Generate ID unik untuk rating
        val ratingId = firestore.collection("ratings").document().id

        // Buat map data rating
        val ratingData = hashMapOf(
            "id" to ratingId,
            "bookId" to bookId,
            "userEmail" to userEmail,
            "rating" to rating,
            "review" to review,
            "recommended" to isRecommended,
            "timestamp" to timestamp
        )

        // Simpan ke Firestore
        firestore.collection("ratings").document(ratingId)
            .set(ratingData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Rating berhasil disimpan", Toast.LENGTH_SHORT).show()

                // Update rating average di dokumen buku
                updateBookRating(bookId)

                // Kembali ke halaman sebelumnya
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBookRating(bookId: String) {
        // Query semua rating untuk buku ini
        firestore.collection("ratings")
            .whereEqualTo("bookId", bookId)
            .get()
            .addOnSuccessListener { documents ->
                var totalRating = 0f
                val ratingsCount = documents.size()

                // Hitung total rating
                for (document in documents) {
                    val ratingValue = document.getDouble("rating")?.toFloat() ?: 0f
                    totalRating += ratingValue
                }

                // Hitung rata-rata
                val averageRating = if (ratingsCount > 0) totalRating / ratingsCount else 0f

                // Update dokumen buku dengan rating rata-rata
                val bookUpdate = hashMapOf(
                    "averageRating" to averageRating,
                    "ratingsCount" to ratingsCount
                )

                firestore.collection("books").document(bookId)
                    .update(bookUpdate as Map<String, Any>)
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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}