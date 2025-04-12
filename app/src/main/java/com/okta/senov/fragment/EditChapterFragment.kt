package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.databinding.FragmentEditChapterBinding

class EditChapterFragment : Fragment() {
    private var _binding: FragmentEditChapterBinding? = null
    private val binding get() = _binding!!

    private var bookId: String? = null
    private var chapterId: Int = 0
    private var bookTitle: String? = null
    private var chapterTitle: String? = null
    private var chapterContent: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditChapterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments
        bookId = arguments?.getString("bookId")
        chapterId = arguments?.getInt("chapterNumber") ?: 0
        bookTitle = arguments?.getString("bookTitle")
        chapterTitle = arguments?.getString("chapterTitle") ?: ""
        chapterContent = arguments?.getString("chapterContent") ?: ""

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.toolbarTitle.text = "Edit Chapter"
        binding.chapterTitleEditText.setText(chapterTitle)
        binding.chapterContentEditText.setText(chapterContent)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.saveButton.setOnClickListener {
            saveChapterChanges()
        }
    }

    private fun saveChapterChanges() {
        val newTitle = binding.chapterTitleEditText.text.toString().trim()
        val newContent = binding.chapterContentEditText.text.toString().trim()

        if (newTitle.isEmpty()) {
            binding.chapterTitleEditText.error = "Title cannot be empty"
            return
        }

        if (newContent.isEmpty()) {
            binding.chapterContentEditText.error = "Content cannot be empty"
            return
        }

        showLoading(true)

        // Update chapter in Firebase
        db.collection("bookContents")
            .whereEqualTo("bookId", bookId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showLoading(false)
                    Toast.makeText(context, "Book content not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = documents.documents[0]
                val chaptersData =
                    document.get("chapters") as? ArrayList<HashMap<String, Any>> ?: arrayListOf()

                // Find and update the specific chapter
                var found = false
                for (i in chaptersData.indices) {
                    val chapter = chaptersData[i]
                    val number = (chapter["number"] as? Long)?.toInt() ?: 0

                    if (number == chapterId) {
                        chaptersData[i] = hashMapOf(
                            "number" to number,
                            "title" to newTitle,
                            "content" to newContent
                        )
                        found = true
                        break
                    }
                }

                if (!found) {
                    showLoading(false)
                    Toast.makeText(context, "Chapter not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Update Firestore document
                db.collection("bookContents").document(document.id)
                    .update("chapters", chaptersData)
                    .addOnSuccessListener {
                        showLoading(false)
                        Toast.makeText(context, "Chapter updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        Toast.makeText(
                            context,
                            "Failed to update: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}