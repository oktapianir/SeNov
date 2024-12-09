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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        setupRecyclerViews()

        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
        binding.searchIcon.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_search)
        }
    }

    private fun setupRecyclerViews() {
        val popularBooksLayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.popularBooksRecyclerView.layoutManager = popularBooksLayoutManager

        bookViewModel.popularBooks.observe(viewLifecycleOwner) { popularBooks ->
            binding.popularBooksRecyclerView.adapter = BookAdapter(popularBooks, isPopular = true)
        }

        val allBooksLayoutManager = LinearLayoutManager(requireContext())
        binding.allBooksRecyclerView.layoutManager = allBooksLayoutManager

        bookViewModel.allBooks.observe(viewLifecycleOwner) { allBooks ->
            binding.allBooksRecyclerView.adapter = BookAdapter(allBooks, isPopular = false)
        }
    }
}
