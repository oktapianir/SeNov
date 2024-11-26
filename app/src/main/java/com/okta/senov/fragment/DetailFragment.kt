package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.okta.senov.databinding.FragmentDetailBinding // Import ViewBinding

class DetailFragment : Fragment() {

    // Deklarasikan binding
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!! // Mendapatkan referensi binding yang aman

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi binding
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        // Setup aksi ketika tombol back ditekan
        setupBackButton()

        return binding.root
    }


    private fun setupBackButton() {
        // Menggunakan OnBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Lakukan sesuatu ketika tombol back ditekan
                    // Contoh: kembali ke fragment sebelumnya
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )

        // Menambahkan aksi pada tombol di fragment
        binding.backButton.setOnClickListener {
            // Trigger back pressed
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null setelah view dihancurkan untuk menghindari memory leak
        _binding = null
    }
}
