package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.okta.senov.R
import com.okta.senov.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set some example data, you can replace it with actual user data
        binding.tvUsername.text = getString(R.string.oktapiani_r)
        binding.tvEmail.text = getString(R.string.ramdhanioktapiani_gmail_com)

        // Menyambungkan ImageButton (icBack) dengan NavController untuk tombol back
        binding.icBack.setOnClickListener {
            // Menggunakan navigateUp() untuk kembali ke fragment sebelumnya
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clear binding reference
    }
}
