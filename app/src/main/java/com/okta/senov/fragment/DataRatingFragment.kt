package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.okta.senov.adapter.RatingAdapter
import com.okta.senov.databinding.FragmentDataRatingBinding
import com.okta.senov.model.Rating
import timber.log.Timber

class DataRatingFragment : Fragment() {

    private var _binding: FragmentDataRatingBinding? = null
    private val binding get() = _binding!!

    private lateinit var ratingAdapter: RatingAdapter
    private val ratingsList = mutableListOf<Rating>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tambahkan listeners untuk chips sorting
        setupSortingChips()

        setupRecyclerView()
        // Default sorting: Highest Rating (sesuai dengan yang checked="true" di XML)
        loadRatingsFromFirebase(
            "rating",
            Query.Direction.DESCENDING
        )        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            // Refresh dengan sorting yang aktif saat ini
            when {
                binding.chipHighestRating.isChecked -> loadRatingsFromFirebase(
                    "rating",
                    Query.Direction.DESCENDING
                )

                binding.chipLowestRating.isChecked -> loadRatingsFromFirebase(
                    "rating",
                    Query.Direction.ASCENDING
                )

                binding.chipNewest.isChecked -> loadRatingsFromFirebase(
                    "timestamp",
                    Query.Direction.DESCENDING
                )

                binding.chipOldest.isChecked -> loadRatingsFromFirebase(
                    "timestamp",
                    Query.Direction.ASCENDING
                )

                else -> loadRatingsFromFirebase("timestamp", Query.Direction.DESCENDING) // Default
            }
        }

    }

    private fun setupRecyclerView() {
        ratingAdapter = RatingAdapter(ratingsList)
        binding.rvRatings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ratingAdapter
        }
    }

    private fun loadRatingsFromFirebase(sortField: String, direction: Query.Direction) {
        // Tampilkan loading indicator jika ada
        // binding.progressBar.visibility = View.VISIBLE

        // Tambahkan tampilan refresh jika menggunakan SwipeRefreshLayout
        binding.swipeRefresh.isRefreshing = true

        db.collection("ratings")
            .orderBy(sortField, direction)
            .get()
            .addOnSuccessListener { documents ->
                Timber.tag("DataRatingFragment").d("Received ${documents.size()} documents")
                ratingsList.clear()

                for (document in documents) {
                    try {
                        // Debug data mentah
                        Timber.tag("DataRatingFragment").d("Document ID: ${document.id}")
                        Timber.tag("DataRatingFragment").d("Raw data: ${document.data}")

                        // Cek nilai timestamp
                        val timestampValue = document.get("timestamp")
                        Timber.tag("DataRatingFragment")
                            .d("Timestamp value: $timestampValue (${timestampValue?.javaClass?.name})")

                        // Konversi ke objek Rating
                        val rating = document.toObject(Rating::class.java)
                        Timber.tag("DataRatingFragment").d("Rating object created: $rating")
                        ratingsList.add(rating)
                    } catch (e: Exception) {
                        Timber.tag("DataRatingFragment")
                            .e(e, "Error processing document ${document.id}: ${e.message}")
                    }
                }

                // Update adapter
                ratingAdapter.notifyDataSetChanged()

                // Sembunyikan loading indicator
                // binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                // Tampilkan pesan jika tidak ada data
                if (ratingsList.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    Timber.tag("DataRatingFragment").d("No ratings found")
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                // Sembunyikan loading indicator
                // binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                Timber.tag("DataRatingFragment")
                    .e(exception, "Error getting documents: ${exception.message}")
                Toast.makeText(
                    context,
                    "Gagal memuat data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupSortingChips() {
        // Set listener untuk setiap chip
        binding.chipHighestRating.setOnClickListener {
            loadRatingsFromFirebase("rating", Query.Direction.DESCENDING)
        }

        binding.chipLowestRating.setOnClickListener {
            loadRatingsFromFirebase("rating", Query.Direction.ASCENDING)
        }

        binding.chipNewest.setOnClickListener {
            loadRatingsFromFirebase("timestamp", Query.Direction.DESCENDING)
        }

        binding.chipOldest.setOnClickListener {
            loadRatingsFromFirebase("timestamp", Query.Direction.ASCENDING)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}