package com.okta.senov.fragment

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
    private var documentId: String? = null
    private var originalStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getString(ARG_DOCUMENT_ID)
            Timber.tag(TAG).d("Received document ID: $documentId")
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

        // Load data if documentId is available
        documentId?.let {
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
    private fun migrateExistingDocumentsToCustomId() {
        val collectionName = "support_requests"

        firestore.collection(collectionName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val oldId = document.id
                    val data = document.data ?: continue

                    // Buat ID baru dengan format yang diinginkan
                    val newId = "idSupport_Request_${System.currentTimeMillis()}"

                    // Salin data ke dokumen baru dengan ID kustom
                    firestore.collection(collectionName).document(newId)
                        .set(data)
                        .addOnSuccessListener {
                            Timber.tag(TAG).d("Document successfully copied to new ID: $newId")

                            // Hapus dokumen lama setelah berhasil menyalin
                            firestore.collection(collectionName).document(oldId)
                                .delete()
                                .addOnSuccessListener {
                                    Timber.tag(TAG).d("Old document successfully deleted: $oldId")
                                }
                                .addOnFailureListener { e ->
                                    Timber.tag(TAG).e(e, "Error deleting old document")
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.tag(TAG).e(e, "Error copying document")
                        }

                    // Tambahkan delay untuk menghindari collision timestamp
                    Thread.sleep(5)
                }
            }
            .addOnFailureListener { e ->
                Timber.tag(TAG).e(e, "Error getting documents")
            }
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

    private fun loadUserAssistanceData(docId: String) {
        showLoading(true)
        Timber.tag(TAG).d("Loading data for document: $docId")

        // Verify the collection name - make sure this matches your Firestore structure
        val collectionName = "support_requests" // Change this if your collection has a different name

        firestore.collection(collectionName).document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Timber.tag(TAG).d("Document data: ${document.data}")

                    // Populate fields with data
                    binding.requestId.text = docId
                    binding.categoryValue.text = document.getString("category") ?: ""
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
                    Timber.tag(TAG).e("Document doesn't exist")
                    showError("Dokumen tidak ditemukan")
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
        val docId = documentId
        if (docId.isNullOrEmpty()) {
            Timber.tag(TAG).e("Cannot save: document ID is null or empty")
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

        Timber.tag(TAG).d("Original status: $originalStatus, New status: $newStatus")

        // Remove this condition to allow saving even if status appears the same
        // Sometimes the formatting differences might cause comparison issues
        /*
        if (convertStatusFormat(originalStatus) == newStatus) {
            Toast.makeText(requireContext(), "Status tidak berubah", Toast.LENGTH_SHORT).show()
            return
        }
        */

        showLoading(true)

        // Verify the collection name - make sure this matches your Firestore structure
        val collectionName = "support_requests" // Change this if your collection has a different name

        Timber.tag(TAG)
            .d("Saving status '$newStatus' to document '$docId' in collection '$collectionName'")

        // Update only the status field
        firestore.collection(collectionName).document(docId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Timber.tag(TAG).d("Status successfully updated")
                showLoading(false)
                Toast.makeText(requireContext(), "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                // Refresh the data to verify the update
                loadUserAssistanceData(docId)
                // Don't navigate back immediately so user can see the change
                // requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                Timber.tag(TAG).e(e, "Error updating status")
                showLoading(false)
                showError("Gagal memperbarui status: ${e.message}")
            }
    }

    // Helper function to convert between status formats
    private fun convertStatusFormat(status: String): String {
        return when (status) {
            "Pending" -> "Menunggu"
            "Processing" -> "Diproses"
            "Completed" -> "Selesai"
            else -> status // Keep as is if already in Indonesian
        }
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss 'UTC'Z", Locale("id"))
        return formatter.format(date)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_DOCUMENT_ID = "document_id"

        @JvmStatic
        fun newInstance(documentId: String) =
            EditDataBantuanPenggunaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DOCUMENT_ID, documentId)
                }
            }
    }
}