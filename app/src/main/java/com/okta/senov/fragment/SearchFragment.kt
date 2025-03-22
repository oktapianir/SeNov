package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.TopAuthorsAdapter
import com.okta.senov.adapter.TopBooksAdapter
import com.okta.senov.databinding.FragmentSearchBinding
import com.okta.senov.extensions.findNavController
import com.okta.senov.model.Book
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var authorAdapter: TopAuthorsAdapter
    private lateinit var bookAdapter: TopBooksAdapter
    private lateinit var recentBookAdapter: TopBooksAdapter

    private val viewModel: BookViewModel by viewModels()

//    private val topAuthors = listOf(
//        Author("Tere Liye", R.drawable.img_andrea_hirata),
//        Author("Dewi Lestari", R.drawable.img_andrea_hirata),
//        Author("Andrea Hirata", R.drawable.img_andrea_hirata),
//        Author("Fiersa Besari", R.drawable.img_andrea_hirata),
//        Author("Leila S.Chudori", R.drawable.img_andrea_hirata),
//        Author("Pidi Baiq", R.drawable.img_andrea_hirata)
//    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        setupAuthorsRecyclerView()

        setupBooksRecyclerView()

        setupRecentBooksRecyclerView()

        setupObservers()

        viewModel.fetchBooksFromApi(getString(R.string.api_key))

        binding.backButton.setOnClickListener {
            binding.backButton.findNavController().navigateUp()
        }
    }

    private fun setupAuthorsRecyclerView() {
        binding.topAuthorsRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        authorAdapter = TopAuthorsAdapter(emptyList())
        binding.topAuthorsRecycler.adapter = authorAdapter
    }

    private fun setupBooksRecyclerView() {
        binding.topBooksRecycler.layoutManager = LinearLayoutManager(context)
        bookAdapter = TopBooksAdapter(emptyList())
        binding.topBooksRecycler.adapter = bookAdapter
    }

    private fun setupRecentBooksRecyclerView() {
        binding.recentBooksRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recentBookAdapter = TopBooksAdapter(emptyList())
        binding.recentBooksRecycler.adapter = recentBookAdapter
    }

    private fun setupObservers() {
//        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
//            bookAdapter.updateBooks(books)
//        }

        viewModel.popularBooks.observe(viewLifecycleOwner) { popularBooks ->
            val recentBooks = popularBooks.map { bookData ->
                Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    description = bookData.description,
                    image = bookData.image
                )
            }
            recentBookAdapter.updateBooks(recentBooks)
        }
    }
}
