package com.okta.senov.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAdminBinding
import com.okta.senov.extensions.findNavController
import timber.log.Timber
import androidx.core.content.edit

class AdminFragment : Fragment(R.layout.fragment_admin) {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.tag("AdminFragment").d("onCreateView called")
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        binding.btnTambahAuthor.setOnClickListener {
            binding.btnTambahAuthor.findNavController().navigate(R.id.action_adminFragment_to_addauthorFragment)
        }
        binding.btnTambahIsiContent.setOnClickListener {
            binding.btnTambahIsiContent.findNavController().navigate(R.id.action_adminFragment_to_addBookFragment)
        }
        binding.btnAddBookContent.setOnClickListener {
            binding.btnAddBookContent.findNavController().navigate(R.id.action_adminFragment_to_addContentBookFragment)
        }
        binding.cardLogout.setOnClickListener {
            logout()
        }
        return binding.root
    }
    private fun logout() {
        try {
            // 1. Hapus token autentikasi
            clearAuthToken()

            // 2. Hapus data sesi sementara
            clearTemporarySessionData()

            // 3. Reset status login
            resetLoginState()

            // 4. Navigasi ke halaman login
            navigateToLoginFragment()

            // 5. Tampilkan pesan logout berhasil
            showLogoutSuccessMessage()

        } catch (e: Exception) {
            Timber.e(e, "Logout failed")
            showLogoutErrorMessage()
        }
    }

    private fun clearAuthToken() {
        // Hapus token autentikasi tetapi simpan informasi akun dasar
        val tokenPrefs = requireContext().getSharedPreferences("AuthTokenPrefs", Context.MODE_PRIVATE)
        tokenPrefs.edit() { remove("access_token").remove("refresh_token") }
    }

    private fun clearTemporarySessionData() {
        // Hapus data sesi sementara
        val sessionPrefs = requireContext().getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE)
        sessionPrefs.edit() {
            remove("last_login_timestamp")
            remove("current_session_id")
            remove("device_info")
            // Tambahkan data sesi sementara lain yang ingin Anda hapus
        }
    }

    private fun resetLoginState() {
        // Reset status login tanpa menghapus informasi akun
        val loginStatePrefs = requireContext().getSharedPreferences("LoginState", Context.MODE_PRIVATE)
        loginStatePrefs.edit() {
            putBoolean("isLoggedIn", false)
            // Tetap simpan username atau informasi akun dasar
            // contoh: putString("username", "namapengguna")
        }
    }

    private fun navigateToLoginFragment() {
        binding.cardLogout.findNavController().navigate(R.id.action_adminFragment_to_loginFragment)
    }

    private fun showLogoutSuccessMessage() {
        Toast.makeText(
            requireContext(),
            "Logout berhasil",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLogoutErrorMessage() {
        Toast.makeText(
            requireContext(),
            "Logout gagal. Silakan coba lagi.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        Timber.tag("AdminFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }
}
