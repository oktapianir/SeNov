package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.okta.senov.databinding.FragmentDetailDataBookBinding

class DetailDataBookFragment : Fragment() {
    private var _binding: FragmentDetailDataBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailDataBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        arguments?.let { args ->
            val bookId = args.getString("idBook", "")
            val bookName = args.getString("titleBook", "")
            val bookAuthor = args.getString("nameAuthor", "")
            val bookCategory = args.getString("nameCategory", "")
            val bookDescription = args.getString("bookDescription", "")
            val imageUrl = args.getString("fotoUrl", "")

            // Tampilkan data ke UI
            displayBookDetails(bookName, bookAuthor, bookCategory, bookDescription, imageUrl)
        }

        // Set up tombol kembali
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun displayBookDetails(
        title: String,
        author: String,
        category: String,
        description: String,
        imageUrl: String
    ) {
        binding.bookNameTextView.text = title
        binding.authorBookTextView.text = author
        binding.categoryTextView.text = category
        binding.descriptionTextView.text = description

        // Load gambar buku menggunakan Glide
        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(com.okta.senov.R.drawable.ic_profile_placeholder) // Ganti dengan placeholder Anda
                .error(com.okta.senov.R.drawable.ic_error) // Ganti dengan gambar error Anda
                .into(binding.bookImageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}