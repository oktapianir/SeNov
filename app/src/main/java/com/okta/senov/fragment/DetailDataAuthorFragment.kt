package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.okta.senov.databinding.FragmentDetailDataAuthorBinding

class DetailDataAuthorFragment : Fragment() {
    private var _binding: FragmentDetailDataAuthorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailDataAuthorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        arguments?.let { args ->
            val authorId = args.getString("authorId", "")
            val nameAuthor = args.getString("nameAuthor", "")
            val socialMedia = args.getString("socialMedia", "")
            val bioAuthor = args.getString("bioAuthor", "")
            val imageUrl = args.getString("imageUrl", "")

            // Tampilkan data ke UI
            displayAuthorDetails(nameAuthor, socialMedia, bioAuthor, imageUrl)
        }

        // Set up tombol kembali
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun displayAuthorDetails(
        name: String,
        socialMedia: String,
        bio: String,
        imageUrl: String
    ) {
        binding.authorNameTextView.text = name
        binding.socialMediaTextView.text = socialMedia
        binding.bioTextView.text = bio

        // Load gambar author menggunakan Glide
        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(com.okta.senov.R.drawable.ic_profile_placeholder) // Ganti dengan placeholder Anda
                .error(com.okta.senov.R.drawable.ic_error) // Ganti dengan gambar error Anda
                .into(binding.authorImageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}