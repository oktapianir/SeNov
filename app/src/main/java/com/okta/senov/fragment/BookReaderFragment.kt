//package com.okta.senov.fragment
//
//import android.os.Bundle
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import com.okta.senov.R
//import com.okta.senov.databinding.FragmentBookReaderBinding
//import com.okta.senov.model.Chapter
//import com.okta.senov.viewmodel.BookViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import timber.log.Timber
//
//@AndroidEntryPoint
//class BookReaderFragment : Fragment(R.layout.fragment_book_reader) {
//
//    private lateinit var binding: FragmentBookReaderBinding
//    private val viewModel: BookViewModel by viewModels()
//    private var currentChapters: List<Chapter> = emptyList()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentBookReaderBinding.bind(view)
//
//        // Ambil data buku dari Safe Args
//        val bookId = BookReaderFragmentArgs.fromBundle(requireArguments()).bookContentArg.bookId
//        val bookTitle = BookReaderFragmentArgs.fromBundle(requireArguments()).bookContentArg.title
//
//        // Set judul toolbar
//        binding.toolbar.title = bookTitle
//        binding.toolbar.setNavigationOnClickListener {
//            requireActivity().onBackPressed()
//        }
//
//        // Setup spinner untuk memilih bab
//        binding.chapterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                if (currentChapters.isNotEmpty() && position < currentChapters.size) {
//                    val chapter = currentChapters[position]
//                    binding.contentTextView.text = chapter.content
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Tidak melakukan apa-apa
//            }
//        }
//
//        // Observe loading state
//        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
//
//        // Observe error state
//        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
//            if (errorMessage.isNotEmpty()) {
//                binding.contentTextView.text = "Error: $errorMessage"
//                Timber.e("Error loading book content: $errorMessage")
//            }
//        }
//
//        // Observe book content
//        viewModel.bookContent.observe(viewLifecycleOwner) { bookContentList ->
//            if (bookContentList.isNotEmpty()) {
//                val bookContent = bookContentList.first()
//                currentChapters = bookContent.chapters
//
//                // Perbarui spinner dengan judul bab
//                val chapterTitles = currentChapters.map { "Bab ${it.number}: ${it.title}" }
//                val adapter = ArrayAdapter(
//                    requireContext(),
//                    android.R.layout.simple_spinner_item,
//                    chapterTitles
//                )
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                binding.chapterSpinner.adapter = adapter
//
//                // Tampilkan bab pertama secara default
//                if (currentChapters.isNotEmpty()) {
//                    binding.contentTextView.text = currentChapters.first().content
//                }
//            }
//        }
//
//        // Muat konten buku
//        viewModel.loadBookContent(bookId)
//    }
//}

package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.okta.senov.R
import com.okta.senov.databinding.FragmentBookReaderBinding
import com.okta.senov.model.Chapter
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BookReaderFragment : Fragment(R.layout.fragment_book_reader) {

    private var _binding: FragmentBookReaderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by viewModels()
    private var currentChapters: List<Chapter> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookReaderBinding.bind(view)

        // Ambil data buku dari Safe Args
        val args = BookReaderFragmentArgs.fromBundle(requireArguments())
        val bookId = args.bookContentArg.bookId
        val bookTitle = args.bookContentArg.title

        // Set judul toolbar
        binding.toolbar.title = bookTitle
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Setup spinner untuk memilih bab
        binding.chapterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (currentChapters.isNotEmpty() && position < currentChapters.size) {
                    binding.contentTextView.text = currentChapters[position].content
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak melakukan apa-apa
            }
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error state
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.contentTextView.text = getString(R.string.error, errorMessage)
                Timber.e("Error loading book content: $errorMessage")
            }
        }

        // Observe book content
        viewModel.bookContent.observe(viewLifecycleOwner) { bookContentList ->
            if (bookContentList.isNotEmpty()) {
                val bookContent = bookContentList.first()
                currentChapters = bookContent.chapters

                if (currentChapters.isNotEmpty()) {
                    val chapterTitles = currentChapters.map { "Bab ${it.number}: ${it.title}" }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        chapterTitles
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.chapterSpinner.adapter = adapter

                    // Tampilkan bab pertama secara default
                    binding.contentTextView.text = currentChapters.first().content
                } else {
                    binding.contentTextView.text = getString(R.string.empty_chapter)
                }
            } else {
                binding.contentTextView.text = getString(R.string.empty_book)
            }
        }

        // Logging & muat konten buku
        Timber.d("Fetching book content for bookId: $bookId")
        viewModel.loadBookContent(bookId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
