package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.okta.senov.databinding.FragmentDetailBinding // Import ViewBinding

class DetailFragment : Fragment() {

    // Deklarasikan binding
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!! // Mendapatkan referensi binding yang aman

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inisialisasi binding
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        // Menggunakan binding untuk mengakses button
        binding.backButton.setOnClickListener {
            // Memanggil onBackPressed dari activity
            requireActivity().onBackPressed() // Tetap menggunakan onBackPressed
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding ke null setelah view dihancurkan untuk menghindari memory leak
        _binding = null
    }
}
