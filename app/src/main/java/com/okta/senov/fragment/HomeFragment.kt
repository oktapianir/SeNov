package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.BookAdapter
import com.okta.senov.databinding.FragmentHomeBinding
import com.okta.senov.viewmodel.BookViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    // Gunakan by viewModels() untuk inisialisasi ViewModel
    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize binding
        binding = FragmentHomeBinding.bind(view)

        // Setup RecyclerViews
        setupRecyclerViews()

        // Tambahkan click listener untuk ikon profil
        binding.profileImage.setOnClickListener {
            // Arahkan ke ProfileFragment
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    private fun setupRecyclerViews() {
        // Popular Books RecyclerView
        val popularBooksLayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.popularBooksRecyclerView.layoutManager = popularBooksLayoutManager

        // Observasi data buku populer dari ViewModel
        bookViewModel.popularBooks.observe(viewLifecycleOwner) { popularBooks ->
            binding.popularBooksRecyclerView.adapter = BookAdapter(popularBooks, isPopular = true)
        }

        // All Books RecyclerView
        val allBooksLayoutManager = LinearLayoutManager(requireContext())
        binding.allBooksRecyclerView.layoutManager = allBooksLayoutManager

        // Observasi semua buku dari ViewModel
        bookViewModel.allBooks.observe(viewLifecycleOwner) { allBooks ->
            binding.allBooksRecyclerView.adapter = BookAdapter(allBooks, isPopular = false)
        }
    }
}
