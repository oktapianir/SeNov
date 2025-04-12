package com.okta.senov.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.adapter.AuthorAdapter
import com.okta.senov.databinding.FragmentAuthorListBinding
import com.okta.senov.model.Author
import timber.log.Timber

class AuthorListFragment : Fragment() {
    private var _binding: FragmentAuthorListBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var authorAdapter: AuthorAdapter
    private val authorList = mutableListOf<Author>()
    private val filteredList = mutableListOf<Author>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupUI()
        loadAuthors()
    }

    private fun setupAdapter() {
        authorAdapter = AuthorAdapter(
            onAuthorClick = { author ->
                // Navigate to author detail fragment
                val bundle = Bundle().apply {
                    putString("idAuthor", author.id)
                    putString("nameAuthor", author.nameAuthor)
                    putString("socialMedia", author.socialMedia)
                    putString("bioAuthor", author.bioAuthor)
                    putString("imageUrl", author.imageUrl)
                }

                // Navigasi ke fragment detail author
                findNavController().navigate(
                    R.id.action_authorListFragment_to_detailDataAuthorFragment,
                    bundle
                )
            },
            onDeleteClick = { author ->
                showDeleteConfirmationDialog(author)
            },
            onEditClick = { author ->
                navigateToEditAuthor(author)
            }
        )

        binding.authorRecyclerView.adapter = authorAdapter
    }

    private fun setupUI() {
        // Setup back button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        // Setup search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAuthors(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    // Add method to navigate to edit screen
    private fun navigateToEditAuthor(author: Author) {
        val bundle = Bundle().apply {
            putString("idAuthor", author.id)
            putString("nameAuthor", author.nameAuthor)
            putString("socialMedia", author.socialMedia)
            putString("bioAuthor", author.bioAuthor)
            putString("imageUrl", author.imageUrl)
        }

        // Navigate to edit chapter fragment
        findNavController().navigate(
            R.id.action_authorListFragment_to_editAuthorFragment,
            bundle
        )
    }


    private fun loadAuthors() {
        showLoading(true)

        db.collection("authors")
            .get()
            .addOnSuccessListener { documents ->
                authorList.clear()

                for (document in documents) {
                    val id = document.id
                    val nameAuthor = document.getString("nameAuthor") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val socialMedia = document.getString("socialMedia") ?: ""
                    val bioAuthor = document.getString("bioAuthor") ?: ""

                    val author = Author(
                        id = id,
                        nameAuthor = nameAuthor,
                        socialMedia = socialMedia,
                        bioAuthor = bioAuthor,
                        imageUrl = imageUrl
                    )

                    authorList.add(author)
                }

                updateUI()
                showLoading(false)
            }
            .addOnFailureListener { e ->
                Timber.e("Error loading authors: ${e.message}")
                showLoading(false)
                showEmptyState(true)
                Toast.makeText(
                    requireContext(),
                    "Failed to load authors: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun filterAuthors(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(authorList)
        } else {
            val searchQuery = query.lowercase()
            filteredList.addAll(authorList.filter {
                it.nameAuthor.lowercase().contains(searchQuery)
            })
        }

        authorAdapter.submitList(filteredList.toList())
        showEmptyState(filteredList.isEmpty())
    }

    private fun updateUI() {
        // Update the adapter with the data
        filteredList.clear()
        filteredList.addAll(authorList)
        authorAdapter.submitList(filteredList.toList())

        // Show empty state if needed
        showEmptyState(authorList.isEmpty())
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.authorRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.authorRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            binding.authorRecyclerView.visibility = View.GONE
            binding.emptyStateView.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmationDialog(author: Author) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Author")
            .setMessage("Are you sure you want to delete ${author.nameAuthor}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteAuthor(author)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAuthor(author: Author) {
        db.collection("authors").document(author.id)
            .delete()
            .addOnSuccessListener {
                // Remove from our list and update UI
                val position = authorList.indexOfFirst { it.id == author.id }
                if (position != -1) {
                    authorList.removeAt(position)
                    updateUI()
                }

                Toast.makeText(
                    requireContext(),
                    "${author.nameAuthor} has been deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Timber.e("Error deleting author: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to delete author: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}