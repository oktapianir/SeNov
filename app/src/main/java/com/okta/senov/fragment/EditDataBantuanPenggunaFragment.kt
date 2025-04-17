package com.okta.senov.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.databinding.FragmentEditDataBantuanPenggunaBinding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class EditDataBantuanPenggunaFragment : Fragment() {

    private val TAG = "EditStatusFragment"

    // View Binding
    private var _binding: FragmentEditDataBantuanPenggunaBinding? = null
    private val binding get() = _binding!!

    // Firebase reference
    private lateinit var firestore: FirebaseFirestore

    // Data
    private var firestoreDocId: String? = null  // ID dokumen di Firestore
    private var customDocId: String? = null     // ID kustom (id_support_request)
    private var originalStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customDocId = it.getString(ARG_DOCUMENT_ID)
            Timber.tag(TAG).d("Received custom document ID: $customDocId")
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize view binding
        _binding = FragmentEditDataBantuanPenggunaBinding.inflate(inflater, container, false)

        // Set up status spinner
        setupSpinner()

        // Set click listeners
        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveButton.setOnClickListener {
            Timber.tag(TAG).d("Save button clicked")
            saveStatusChanges()
        }

        // Load data if customDocId is available
        customDocId?.let {
            if (it.isNotEmpty()) {
                loadUserAssistanceData(it)
            } else {
                showError("ID dokumen tidak valid")
            }
        } ?: run {
            showError("ID dokumen tidak ditemukan")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSpinner() {
        // Create array adapter for status options
        val statusOptions = arrayOf("Menunggu", "Diproses", "Selesai")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statusSpinner.adapter = adapter
    }

    private fun loadUserAssistanceData(customId: String) {
        showLoading(true)
        Timber.tag(TAG).d("Loading data for custom ID: $customId")

        // Verify the collection name - make sure this matches your Firestore structure
        val collectionName = "support_requests" // Change this if your collection has a different name

        // Menggunakan query untuk menemukan dokumen berdasarkan field id_support_request
        firestore.collection(collectionName)
            .whereEqualTo("id_support_request", customId)
            .limit(1)  // Hanya ambil 1 dokumen pertama yang cocok
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    // Simpan ID dokumen Firestore yang sebenarnya
                    firestoreDocId = document.id

                    Timber.tag(TAG).d("Document found with Firestore ID: $firestoreDocId")
                    Timber.tag(TAG).d("Document data: ${document.data}")

                    // Populate fields with data
                    binding.requestId.text = document.getString("id_support_request") ?: ""
                    binding.descriptionValue.text = document.getString("description") ?: ""
                    binding.emailValue.text = document.getString("email") ?: ""
                    binding.nameValue.text = document.getString("name") ?: ""
                    binding.timestampValue.text = formatTimestamp(document.getTimestamp("timestamp"))

                    // Set original status and select current status in spinner
                    originalStatus = document.getString("status") ?: "Pending"
                    Timber.tag(TAG).d("Original status: $originalStatus")
                    setSpinnerSelection(originalStatus)

                    showLoading(false)
                } else {
                    Timber.tag(TAG).e("Document doesn't exist with custom ID: $customId")
                    showError("Dokumen tidak ditemukan")
                    showLoading(false)
                }
            }
            .addOnFailureListener { e ->
                Timber.tag(TAG).e(e, "Error getting document")
                showLoading(false)
                showError("Error memuat data: ${e.message}")
            }
    }

    private fun setSpinnerSelection(status: String) {
        val position = when (status) {
            "Pending", "Menunggu" -> 0
            "Processing", "Diproses" -> 1
            "Completed", "Selesai" -> 2
            else -> 0 // Default to "Menunggu" if unknown
        }
        Timber.tag(TAG).d("Setting spinner position to $position for status: $status")
        binding.statusSpinner.setSelection(position)
    }

    private fun saveStatusChanges() {
        val docId = firestoreDocId  // Gunakan ID Firestore yang sudah disimpan
        if (docId.isNullOrEmpty()) {
            Timber.tag(TAG).e("Cannot save: Firestore document ID is null or empty")
            showError("ID dokumen tidak valid")
            return
        }

        // Get the selected status
        val newStatus = when (binding.statusSpinner.selectedItemPosition) {
            0 -> "Menunggu"
            1 -> "Diproses"
            2 -> "Selesai"
            else -> "Menunggu"
        }

        Timber.tag(TAG).d("Updating document with Firestore ID: $docId")
        Timber.tag(TAG).d("Original status: $originalStatus, New status: $newStatus")

        showLoading(true)

        val collectionName = "support_requests"

        // Update document menggunakan ID dokumen Firestore
        firestore.collection(collectionName).document(docId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Timber.tag(TAG).d("Status successfully updated")
                showLoading(false)
                Toast.makeText(requireContext(), "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()

                // Update original status variable setelah update berhasil
                originalStatus = newStatus

                // Tidak perlu reload karena kita sudah memperbarui status lokal
                // Jika ingin memverifikasi dari server, uncomment baris di bawah
                // loadUserAssistanceData(binding.requestId.text.toString())
            }
            .addOnFailureListener { e ->
                Timber.tag(TAG).e(e, "Error updating status")
                showLoading(false)
                showError("Gagal memperbarui status: ${e.message}")
            }
        statusUpdateCallback?.onStatusUpdated()

    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss 'UTC'Z", Locale("id"))
        return formatter.format(date)
    }
    interface StatusUpdateCallback {
        fun onStatusUpdated()
    }

    private var statusUpdateCallback: StatusUpdateCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Coba dapatkan callback dari parent fragment
        parentFragment?.let {
            if (it is StatusUpdateCallback) {
                statusUpdateCallback = it
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_DOCUMENT_ID = "id_support_request"

        @JvmStatic
        fun newInstance(documentId: String) =
            EditDataBantuanPenggunaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DOCUMENT_ID, documentId)
                }
            }
    }
}