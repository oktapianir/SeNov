package com.okta.senov.fragment

//@AndroidEntryPoint
//class HomeFragment : Fragment(R.layout.fragment_home) {
//
//    private lateinit var binding: FragmentHomeBinding
//    private val bookViewModel: BookViewModel by viewModels()
//
//    private val apiKey = "8b71325fbf3a43d8a949fd23ce4e2f5a"
//
//    private lateinit var allBooksAdapter: AllBooksAdapter
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentHomeBinding.bind(view)
//
//        setupRecyclerViews()
//
//        binding.profileImage.setOnClickListener {
//            binding.profileImage.findNavController().navigate(R.id.action_home_to_profile)
//        }
//
//        binding.searchIcon.setOnClickListener {
//            binding.searchIcon.findNavController().navigate(R.id.action_home_to_search)
//        }
//        binding.yourBook.setOnClickListener {
//            binding.yourBook.findNavController().navigate(R.id.action_home_to_yourbook)
//        }
//
//        bookViewModel.fetchBooksFromApi(apiKey, "adventure")
//
////        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
////            val adapter = BookAdapter(books)
////            binding.popularBooksRecyclerView.adapter = adapter
////        }
////        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
////            val adapter = BookAdapter(books) { book ->
////                val action = HomeFragmentDirections.actionHomeToDetail(book)
////                binding.popularBooksRecyclerView.findNavController().navigate(action)
////            }
////            binding.popularBooksRecyclerView.adapter = adapter
////        }
//        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
//            val adapter = BookAdapter(books) { bookData ->
//                // Convert BookData to Book
//                val book = Book(
//                    id = bookData.id,
//                    title = bookData.title,
////                    author = bookData.author ?: "",
////                    category = bookData.category ?: "",
////                    description = bookData.description ?: "",
////                    price = bookData.price ?: 0.0,
////                    rating = bookData.rating ?: 0.0f,
////                    authorName = bookData.authorName,
//                    coverResourceId = bookData.image
//                )
//                val action = HomeFragmentDirections.actionHomeToDetail(book)
//                binding.popularBooksRecyclerView.findNavController().navigate(action)
//            }
//            binding.popularBooksRecyclerView.adapter = adapter
//        }
//
//        bookViewModel.allBooks.observe(viewLifecycleOwner) { books ->
//            allBooksAdapter = AllBooksAdapter(books) { book ->
//                val action = HomeFragmentDirections.actionHomeToDetail(book)
//                binding.allBooksRecyclerView.findNavController().navigate(action)
//            }
//            binding.allBooksRecyclerView.adapter = allBooksAdapter
//        }
//    }
//
//    private fun setupRecyclerViews() {
//        binding.popularBooksRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        binding.allBooksRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//    }
//
//}

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
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val bookViewModel: BookViewModel by viewModels()

    private val apiKey = "8b71325fbf3a43d8a949fd23ce4e2f5a"

    // Adapter untuk daftar buku populer
    private lateinit var bookAdapter: BookAdapter

    // Adapter untuk semua buku
    private lateinit var allBooksAdapter: AllBooksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        setupRecyclerViews()

        // Inisialisasi Adapter
        bookAdapter = BookAdapter{ bookData ->
            val book = Book(
                id = bookData.id,
                title = bookData.title,
                authorName = bookData.authorName,
                category = bookData.category,
                description = bookData.description,
                image = bookData.image
            )
            val action = HomeFragmentDirections.actionHomeToDetail(book)
//            binding.popularBooksRecyclerView.findNavController().navigate(action)
        }

        allBooksAdapter = AllBooksAdapter(emptyList()) { book ->
            val action = HomeFragmentDirections.actionHomeToDetail(book)
            binding.allBooksRecyclerView.findNavController().navigate(action)
        }


//        binding.popularBooksRecyclerView.adapter = bookAdapter
        binding.allBooksRecyclerView.adapter = allBooksAdapter

        // Fetch data dari API
        bookViewModel.fetchBooksFromApi(apiKey, "adventure")

        // Observasi data populer dan update adapter
        bookViewModel.popularBooks.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
        }

        // Observasi semua buku dan update adapter
        bookViewModel.allBooks.observe(viewLifecycleOwner) { bookDataList ->
            // Add logging
            Timber.tag("HOME_FRAGMENT").d("Received ${bookDataList.size} books")

            val books = bookDataList.map { bookData ->
                // Log each conversion
                Timber.tag("DATA_CONVERSION").d("Converting: ID=${bookData.id}, Title=${bookData.title}")

                Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    description = bookData.description,
                    image = bookData.image
                )
            }

            // Log the converted books
            Timber.tag("ADAPTER_UPDATE").d("Setting ${books.size} books to adapter")

            // Update adapter
            allBooksAdapter.setBooks(books)
        }
        // Navigasi ke halaman lain
        binding.profileImage.setOnClickListener {
            binding.profileImage.findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.findNavController().navigate(R.id.action_home_to_search)
        }

        binding.yourBook.setOnClickListener {
            binding.yourBook.findNavController().navigate(R.id.action_home_to_yourbook)
        }
    }

    private fun setupRecyclerViews() {
//        binding.popularBooksRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.allBooksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }
}
