package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.databinding.FragmentAddContentBookBinding
import com.okta.senov.model.BookContent
import com.okta.senov.model.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddContentBookFragment : Fragment() {
    private var _binding: FragmentAddContentBookBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddChapter.setOnClickListener {
            val bookId = binding.etBookId.text.toString().trim()
            val chapterNumber = binding.etChapterNumber.text.toString().trim().toIntOrNull()
            val chapterTitle = binding.etChapterTitle.text.toString().trim()
            val chapterContent = binding.etChapterContent.text.toString().trim()

            if (bookId.isNotEmpty() && chapterNumber != null && chapterTitle.isNotEmpty() && chapterContent.isNotEmpty()) {
                val newChapter = Chapter(
                    number = chapterNumber,
                    title = chapterTitle,
                    content = chapterContent
                )

                CoroutineScope(Dispatchers.IO).launch {
                    addChapterToBook(bookId, newChapter)
                }
            } else {
                Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
            }
            clearFields()
        }
    }

    private suspend fun addChapterToBook(bookId: String, chapter: Chapter) {
        try {
            val docRef = db.collection("bookContents").document(bookId)
            val bookSnapshot = docRef.get().await()

            if (bookSnapshot.exists()) {
                val bookContent = bookSnapshot.toObject(BookContent::class.java)
                val updatedChapters = bookContent?.chapters?.toMutableList() ?: mutableListOf()
                updatedChapters.add(chapter)

                docRef.update("chapters", updatedChapters).await()
            } else {
                val newBookContent = BookContent(
                    bookId = bookId,
                    title = "Judul Tidak Diketahui",
                    chapters = listOf(chapter)
                )
                docRef.set(newBookContent).await()
            }

            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Konten berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Gagal menambahkan konten!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun clearFields() {
        binding.etBookId.text?.clear()
        binding.etChapterNumber.text?.clear()
        binding.etChapterTitle.text?.clear()
        binding.etChapterContent.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
