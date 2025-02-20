package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentRegisterBinding
import timber.log.Timber

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.icBack.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_fragmentProfile)
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Harap masukkan email dan password", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password, "user") // Default role "user"
            }
        }

//        binding.registerButton.setOnClickListener {
//            val email = binding.emailEditText.text.toString()
//            val password = binding.passwordEditText.text.toString()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(
//                    requireContext(),
//                    "Please enter both email and password",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                auth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(requireActivity()) { task ->
//                        if (task.isSuccessful) {
//                            val user = auth.currentUser
//                            Toast.makeText(
//                                requireContext(),
//                                "Account created! Welcome ${user?.email}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
//
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "Registration failed. ${task.exception?.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//            }
//        }

        return binding.root
    }
//    private fun registerUser(email: String, password: String, role: String) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener(requireActivity()) { task ->
//                if (task.isSuccessful) {
//                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
//                    val userMap = hashMapOf(
//                        "email" to email,
//                        "role" to role
//                    )
//
//                    db.collection("users").document(userId).set(userMap)
//                        .addOnSuccessListener {
//                            Timber.d("Role berhasil disimpan")
//                            Toast.makeText(requireContext(), "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
//                            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Firebase").e(e, "Gagal menyimpan role")
//                            Toast.makeText(requireContext(), "Gagal menyimpan data pengguna.", Toast.LENGTH_SHORT).show()
//                        }
//                } else {
//                    Timber.tag("Firebase").e(task.exception, "Registrasi gagal")
//                    Toast.makeText(requireContext(), "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }

    private fun registerUser(email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "email" to email,
                        "role" to role // Bisa "admin", "user", dll.
                    )

                    // Simpan data ke Firestore
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            Timber.tag("Firebase").d("Role berhasil disimpan")

                            // Ambil role dari Firestore
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val userRole = document.getString("role")
                                        if (userRole == "admin") {
                                            findNavController().navigate(R.id.action_registerFragment_to_adminFragment)
                                        } else {
                                            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Timber.tag("Firebase").e(e, "Gagal mengambil role")
                                    Toast.makeText(requireContext(), "Gagal mengambil data pengguna.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("Firebase").e(e, "Gagal menyimpan role")
                            Toast.makeText(requireContext(), "Gagal menyimpan data pengguna.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Timber.tag("Firebase").e(task.exception, "Registrasi gagal")
                    Toast.makeText(requireContext(), "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
