package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.BookAdapter
import com.okta.senov.databinding.FragmentYourBookBinding
import com.okta.senov.model.Book
import com.okta.senov.viewmodel.YourBookViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class YourBookFragment : Fragment(R.layout.fragment_your_book) {

    private var _binding: FragmentYourBookBinding? = null
    private val binding get() = _binding!!

    private val yourBookViewModel: YourBookViewModel by viewModels()
    private lateinit var adapter: BookAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentYourBookBinding.bind(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        refreshData()

        Timber.tag("YourBookFragment")
            .d("onViewCreated: Refreshed books count = ${yourBookViewModel.yourBooks.value?.size ?: 0}")
    }
    // Menambahkan method untuk memperbarui data
    private fun refreshData() {
        yourBookViewModel.fetchBookmarkedBooks()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat fragment kembali menjadi aktif
        refreshData()
        Timber.tag("YourBookFragment")
            .d("onResume: Refreshing books data")
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = BookAdapter(
            onItemClick = { bookData ->
                val book = Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    description = bookData.description,
                    image = bookData.image
                )
                val action = YourBookFragmentDirections.actionYourbookToDetail(book)
                findNavController().navigate(action)
            },
//            onRemoveClick = { book ->
//                yourBookViewModel.removeBook(book.id)
//            }
            onRemoveClick = { book ->
                yourBookViewModel.removeBook(book.id)
                // Langsung refresh data setelah menghapus bookmark
                refreshData()
            }
        )

        binding.yourBookRecyclerView.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            adapter = this@YourBookFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        yourBookViewModel.yourBooks.observe(viewLifecycleOwner) { bookDataList ->
            // Submit list to adapter
            adapter.submitList(bookDataList)

            // Handle empty state
            if (bookDataList.isEmpty()) {
                binding.yourBookRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.yourBookRecyclerView.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
