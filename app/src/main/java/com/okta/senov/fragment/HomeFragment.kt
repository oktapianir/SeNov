package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.AllBooksAdapter
import com.okta.senov.adapter.BookAdapter
import com.okta.senov.databinding.FragmentHomeBinding
import com.okta.senov.extensions.findNavController
import com.okta.senov.model.Book
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val bookViewModel: BookViewModel by viewModels()

    private val apiKey = "8b71325fbf3a43d8a949fd23ce4e2f5a"

    private lateinit var allBooksAdapter: AllBooksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        setupRecyclerViews()

        binding.profileImage.setOnClickListener {
            binding.profileImage.findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.findNavController().navigate(R.id.action_home_to_search)
        }
        binding.yourBook.setOnClickListener {
            binding.yourBook.findNavController().navigate(R.id.action_home_to_yourbook)
        }

        bookViewModel.fetchBooksFromApi(apiKey, "adventure")

//        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
//            val adapter = BookAdapter(books)
//            binding.popularBooksRecyclerView.adapter = adapter
//        }
//        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
//            val adapter = BookAdapter(books) { book ->
//                val action = HomeFragmentDirections.actionHomeToDetail(book)
//                binding.popularBooksRecyclerView.findNavController().navigate(action)
//            }
//            binding.popularBooksRecyclerView.adapter = adapter
//        }
        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
            val adapter = BookAdapter(books) { bookData ->
                // Convert BookData to Book
                val book = Book(
                    id = bookData.id,
                    title = bookData.title,
//                    author = bookData.author ?: "",
//                    category = bookData.category ?: "",
//                    description = bookData.description ?: "",
//                    price = bookData.price ?: 0.0,
//                    rating = bookData.rating ?: 0.0f,
                    coverResourceId = bookData.image
                )
                val action = HomeFragmentDirections.actionHomeToDetail(book)
                binding.popularBooksRecyclerView.findNavController().navigate(action)
            }
            binding.popularBooksRecyclerView.adapter = adapter
        }

        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
            allBooksAdapter = AllBooksAdapter(books) { book ->
                val action = HomeFragmentDirections.actionHomeToDetail(book)
                binding.allBooksRecyclerView.findNavController().navigate(action)
            }
            binding.allBooksRecyclerView.adapter = allBooksAdapter
        }
    }

    private fun setupRecyclerViews() {
        binding.popularBooksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.allBooksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

}
