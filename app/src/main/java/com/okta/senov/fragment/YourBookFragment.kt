package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    // Use by viewModels() instead of activityViewModels() to ensure consistency with Hilt
    private val yourBookViewModel: YourBookViewModel by viewModels()
    private lateinit var adapter: BookAdapter
//    private lateinit var adapter: YourBookAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentYourBookBinding.bind(view)
        setupRecyclerView()
        binding.icBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }


        // Then set up observation
        observeViewModel()

        Timber.tag("YourBookFragment")
            .d("onViewCreated: Refreshed books count = ${yourBookViewModel.yourBooks.value?.size ?: 0}")
    }

//    private fun setupRecyclerView() {
//        adapter = BookAdapter { book ->
//            Toast.makeText(requireContext(), "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.yourBookRecyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = this@YourBookFragment.adapter
//            setHasFixedSize(true)
//        }
//    }

    private fun setupRecyclerView() {
        adapter = BookAdapter(
//            onItemClick = { book ->
//                // Navigate to detail fragment
//                val action = YourBookFragmentDirections.actionYourbookToDetail(book)
//                findNavController().navigate(action)
//            },
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
            onRemoveClick = { book ->
                // Remove book from bookmarks
                yourBookViewModel.removeBook(book.id)
            }
        )

        binding.yourBookRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@YourBookFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        yourBookViewModel.yourBooks.observe(viewLifecycleOwner) { bookDataList ->
            val books = bookDataList.map { bookData ->
                Book(
                    id = bookData.id,
                    title = bookData.title,
                    authorName = bookData.authorName,
                    category = bookData.category,
                    description = bookData.description
                )
            }
            adapter.submitList(bookDataList)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
