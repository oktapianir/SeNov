package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.AllBooksAdapter
import com.okta.senov.adapter.BookAdapter
import com.okta.senov.databinding.FragmentHomeBinding
import com.okta.senov.model.Book

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

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
        binding.popularBooksRecyclerView.adapter = BookAdapter(getPopularBooks())

        // All Books RecyclerView
        val allBooksLayoutManager = LinearLayoutManager(requireContext())
        binding.allBooksRecyclerView.layoutManager = allBooksLayoutManager
        binding.allBooksRecyclerView.adapter = AllBooksAdapter(getAllBooks()) { book ->
            // Handle click on a book
            val action = HomeFragmentDirections.actionHomeToDetail(book.title)
            findNavController().navigate(action)
        }
    }

    // Dummy data for example (Replace with actual data)
    private fun getPopularBooks(): List<Book> {
        return listOf(
            Book("Bulan", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99", R.drawable.img_book_cover1),
            Book("Si Putih", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99", R.drawable.img_book_cover2),
            Book("Bumi", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99", R.drawable.img_book_cover3)
        )
    }

    private fun getAllBooks(): List<Book> {
        return listOf(
            Book("Nebula", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99", R.drawable.img_book_cover4),
            Book("Selena", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99", R.drawable.img_book_cover5),
            Book("Ceroz & Batozar", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99", R.drawable.img_book_cover6),
        )
    }
}