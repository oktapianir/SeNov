package com.okta.senov.fragment

//
////class YourBookFragment : Fragment() {
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////        // Inflate the layout for this fragment
////        return inflater.inflate(R.layout.fragment_your_book, container, false)
////    }
////}
//
////@AndroidEntryPoint
////class YourBookFragment : Fragment(R.layout.fragment_your_book) {
////
////    private lateinit var binding: FragmentYourBookBinding
////    private val yourBookViewModel: YourBookViewModel by viewModels()
////
////    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
////        super.onViewCreated(view, savedInstanceState)
////
////        binding = FragmentYourBookBinding.bind(view)
//////
//////        val adapter = BookAdapter(emptyList())  // Adapter untuk menampilkan daftar buku
////        val adapter = BookAdapter(emptyList()) { book ->
////            // Handle klik item di sini, misalnya pindah ke detail buku
////            Toast.makeText(requireContext(), "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
////        }
////
////        binding.yourBookRecyclerView.layoutManager = LinearLayoutManager(requireContext())
////        binding.yourBookRecyclerView.adapter = adapter
////
////        // Observe perubahan data di ViewModel dan update RecyclerView
////        yourBookViewModel.yourBooks.observe(viewLifecycleOwner) { books ->
////            Log.d("YourBookFragment", "Total books: ${books.size}")
////            adapter.updateBooks(books)
////        }
////    }
////}
//
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.okta.senov.R
//import com.okta.senov.adapter.BookAdapter
//import com.okta.senov.databinding.FragmentYourBookBinding
//import com.okta.senov.viewmodel.YourBookViewModel
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class YourBookFragment : Fragment(R.layout.fragment_your_book) {
//
//    private var _binding: FragmentYourBookBinding? = null
//    private val binding get() = _binding!!
//
//    private val yourBookViewModel: YourBookViewModel by viewModels()
//    private lateinit var adapter: BookAdapter
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        _binding = FragmentYourBookBinding.bind(view)
//
//        adapter = BookAdapter(emptyList()) { book ->
//            Toast.makeText(requireContext(), "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.yourBookRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.yourBookRecyclerView.adapter = adapter
//
//        // Observe perubahan data di ViewModel dan update RecyclerView
//        yourBookViewModel.yourBooks.observe(viewLifecycleOwner) { books ->
//            Log.d("YourBookFragment", "Total books: ${books.size}")
//            adapter.updateBooks(books)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//

//@AndroidEntryPoint
//class YourBookFragment : Fragment(R.layout.fragment_your_book) {
//
//    private var _binding: FragmentYourBookBinding? = null
//    private val binding get() = _binding!!
//
//    private val yourBookViewModel: YourBookViewModel by viewModels()
//    private lateinit var adapter: BookAdapter
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        _binding = FragmentYourBookBinding.bind(view)
//
//        adapter = BookAdapter { book ->
//            Toast.makeText(requireContext(), "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.yourBookRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.yourBookRecyclerView.adapter = adapter
//
//        // Observe LiveData untuk memperbarui RecyclerView
//        yourBookViewModel.yourBooks.observe(viewLifecycleOwner) { books ->
//            Log.d("YourBookFragment", "Total books: ${books.size}")
//            adapter.submitList(books) // 🔥 Gunakan submitList agar daftar diperbarui
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.BookAdapter
import com.okta.senov.databinding.FragmentYourBookBinding
import com.okta.senov.extensions.findNavController
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

    private fun setupRecyclerView() {
        adapter = BookAdapter { book ->
            Toast.makeText(requireContext(), "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
        }

        binding.yourBookRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@YourBookFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        yourBookViewModel.yourBooks.observe(viewLifecycleOwner) { bookDataList ->
            Timber.tag("YourBookFragment").d("Books updated from LiveData: ${bookDataList.size}")

            if (bookDataList.isEmpty()) {
                Timber.tag("YourBookFragment").d("WARNING: Empty book list received from ViewModel")
            } else {
                bookDataList.forEach {
                    Timber.tag("YourBookFragment").d("Book in list: ${it.title}, ID: ${it.id}")
                }
            }

            adapter.submitList(bookDataList)

            // Update UI visibility based on list content
            if (bookDataList.isEmpty()) {
                binding.yourBookRecyclerView.visibility = View.GONE
                // If you have an empty state view, show it here
                // binding.emptyStateView.visibility = View.VISIBLE
            } else {
                binding.yourBookRecyclerView.visibility = View.VISIBLE
                // If you have an empty state view, hide it here
                // binding.emptyStateView.visibility = View.GONE
            }
            // Navigasi ke halaman lain
            binding.yourBookRecyclerView.setOnClickListener {
                binding.yourBookRecyclerView.findNavController()
                    .navigate(R.id.action_yourbook_to_home)
            }
//            yourBooksAdapter.setOnRemoveClickListener { book ->
//                bookViewModel.removeBook(book.id)
//            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
