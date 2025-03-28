package com.okta.senov.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentProfileBinding
import com.okta.senov.databinding.FragmentSupportContactBinding
import com.okta.senov.model.SupportRequest

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var auth: FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
        }

        binding.contactSupportButton.setOnClickListener {
            showSupportBottomSheet()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth?.currentUser
        if (currentUser != null) {
            binding.tvUsername.text = currentUser.displayName ?: "Anonymous"
            binding.tvEmail.text = currentUser.email ?: "Email tidak tersedia"

            // Sembunyikan tombol login dan register
            binding.loginButtonsContainer.visibility = View.GONE
        } else {
            binding.tvUsername.text = getString(R.string.guest)
            binding.tvEmail.text = getString(R.string.opener)

            // Tampilkan tombol login dan register
            binding.loginButtonsContainer.visibility = View.VISIBLE
        }

        binding.icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.icLogout.setOnClickListener {
            auth?.signOut()
            Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        setupFAQInteractions()
    }

    private fun setupFAQInteractions() {
        // FAQ Item 1
        binding.faqHeader1.setOnClickListener {
            toggleFAQAnswer(
                answerView = binding.faqAnswer1,
                arrowView = binding.faqArrow1
            )
        }
    }

    @SuppressLint("UseKtx")
    private fun toggleFAQAnswer(answerView: View, arrowView: View) {
        val isCurrentlyVisible = answerView.visibility == View.VISIBLE

        // Toggle visibilitas
        answerView.visibility = if (isCurrentlyVisible) View.GONE else View.VISIBLE

        // Animasi rotasi panah
        arrowView.animate()
            .rotation(if (isCurrentlyVisible) 0f else 180f)
            .setDuration(300)
            .start()

        // Animasi fade
        answerView.animate()
            .alpha(if (isCurrentlyVisible) 0f else 1f)
            .setDuration(300)
            .start()
    }

    private fun validateForm(name: String, email: String, description: String): Boolean {
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isBlank()) {
            Toast.makeText(requireContext(), "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (description.isBlank()) {
            Toast.makeText(requireContext(), "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun showSupportBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = FragmentSupportContactBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Setup kategori masalah
        val categories = listOf(
            "Pertanyaan Umum",
            "Masalah Akun",
            "Teknis",
            "Pembayaran",
            "Lainnya"
        )
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
//        bottomSheetBinding.spinnerKategori.adapter = categoryAdapter

        // Tombol kirim formulir
        bottomSheetBinding.buttonKirim.setOnClickListener {
            val name = bottomSheetBinding.editTextNama.text.toString()
            val email = bottomSheetBinding.editTextEmail.text.toString()
            val category = bottomSheetBinding.spinnerKategori.selectionStart.toString()
            val description = bottomSheetBinding.editTextDeskripsi.text.toString()

            if (validateForm(name, email, description)) {
                sendSupportRequestToFirebase(
                    SupportRequest(
                        name = name,
                        email = email,
                        category = category,
                        description = description
                    )
                )
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()
    }

    private fun sendSupportRequestToFirebase(supportRequest: SupportRequest) {
        val db = FirebaseFirestore.getInstance()

        db.collection("support_requests")
            .add(supportRequest)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Permintaan anda berhasil dikirim",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Gagal mengirim permintaan: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
