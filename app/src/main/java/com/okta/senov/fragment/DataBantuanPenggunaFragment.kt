package com.okta.senov.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.adapter.SupportRequestAdapter
import com.okta.senov.databinding.FragmentDataBantuanPenggunaBinding
import com.okta.senov.model.SupportRequest
import java.util.Date
import com.google.firebase.firestore.ListenerRegistration

class DataBantuanPenggunaFragment : Fragment(), EditDataBantuanPenggunaFragment.StatusUpdateCallback {

    private var _binding: FragmentDataBantuanPenggunaBinding? = null
    private val binding get() = _binding!!

    private lateinit var supportRequestAdapter: SupportRequestAdapter
    private var supportRequests = listOf<SupportRequest>()
    private val db = FirebaseFirestore.getInstance()
    private var supportRequestsListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBantuanPenggunaBinding.inflate(inflater, container, false)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        setupSortingSpinner()
        loadSupportRequests()

        return binding.root
    }

    private fun setupRecyclerView() {
        supportRequestAdapter = SupportRequestAdapter(
            onEditClick = { supportRequest ->
                navigateToEditBantuan(supportRequest)
            }
        )

        binding.bantuanRecyclerView.apply {
            adapter = supportRequestAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    // Add method to navigate to edit screen
    private fun navigateToEditBantuan(SupportRequest: SupportRequest) {
        val bundle = Bundle().apply {
            putString("id_support_request", SupportRequest.id_support_request)
            putString("name", SupportRequest.name)
            putString("email", SupportRequest.email)
            putString("description", SupportRequest.description)
            putString("status", SupportRequest.status)
//            putString("category", SupportRequest.category)
        }

        // Navigate to edit chapter fragment
        findNavController().navigate(
            R.id.action_dataBantuanPenggunaFragment_to_editDataBantuanPenggunaFragment,
            bundle
        )
    }


    private fun loadSupportRequests() {
        binding.progressBar.visibility = View.VISIBLE

        // Batalkan listener sebelumnya jika ada
        supportRequestsListener?.remove()
        supportRequestsListener = null

        // Buat listener baru
        supportRequestsListener = db.collection("support_requests")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyStateView.visibility = View.VISIBLE
                    binding.bantuanRecyclerView.visibility = View.GONE
                    binding.emptyTextView.text = "Error: ${e.message}"
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyStateView.visibility = View.VISIBLE
                    binding.bantuanRecyclerView.visibility = View.GONE
                    return@addSnapshotListener
                }

                binding.progressBar.visibility = View.GONE

                this.supportRequests = snapshots.documents.mapNotNull { document ->
                    val id = document.getString("id_support_request") ?: ""
//                    val category = document.getString("category") ?: ""
                    val description = document.getString("description") ?: ""
                    val email = document.getString("email") ?: ""
                    val name = document.getString("name") ?: ""
                    val status = document.getString("status") ?: ""
                    val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()

                    SupportRequest(
                        id_support_request = id,
//                        category = category,
                        description = description,
                        email = email,
                        name = name,
                        status = status,
                        timestamp = timestamp
                    )
                }

                binding.emptyStateView.visibility = View.GONE
                binding.bantuanRecyclerView.visibility = View.VISIBLE
                supportRequestAdapter.updateData(this.supportRequests)

                // Jika ada filter yang aktif, terapkan kembali
                val selectedStatus = binding.statusSpinner.selectedItem.toString()
                if (selectedStatus != "Semua") {
                    filterBantuanByStatus(selectedStatus)
                }
            }
    }

    private fun setupSortingSpinner() {
        // Create an array of status options
        val statusOptions = arrayOf("Semua", "Pending", "Diproses", "Selesai")

        // Create an adapter for the spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusOptions
        )

        // Set dropdown layout style
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.statusSpinner.adapter = spinnerAdapter

        // Set listener for item selection
        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedStatus = statusOptions[position]
                filterBantuanByStatus(selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default behavior when nothing is selected
            }
        }
    }

    private fun filterBantuanByStatus(status: String) {
        binding.progressBar.visibility = View.VISIBLE

        if (status == "Semua") {
            // Show all items
            supportRequestAdapter.updateData(supportRequests)
        } else {
            // Filter items by selected status
            val filteredList = supportRequests.filter { supportRequests ->
                supportRequests.status == status
            }
            supportRequestAdapter.updateData(filteredList)
        }

        // Update empty state visibility
        updateEmptyState(supportRequestAdapter.itemCount == 0)
        binding.progressBar.visibility = View.GONE
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.bantuanRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.bantuanRecyclerView.visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        if (supportRequestsListener == null) {
            loadSupportRequests()
        }
    }
    override fun onStatusUpdated() {
        loadSupportRequests() // Refresh data
    }


    override fun onDestroyView() {
        super.onDestroyView()
        supportRequestsListener?.remove()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = DataBantuanPenggunaFragment()
    }
}
