package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAdminBinding
import com.okta.senov.extensions.findNavController
import timber.log.Timber

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
        return binding.root
    }

    override fun onDestroyView() {
        Timber.tag("AdminFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }
}
