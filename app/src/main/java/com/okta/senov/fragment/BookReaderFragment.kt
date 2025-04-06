package com.okta.senov.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.okta.senov.R
import com.okta.senov.databinding.FragmentBookReaderBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import com.okta.senov.model.BookData
import com.okta.senov.model.Chapter
import com.okta.senov.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class BookReaderFragment : Fragment(R.layout.fragment_book_reader) {
    // Variabel binding untuk menghubungkan tampilan (UI) dengan kode
    private var _binding: FragmentBookReaderBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk mengelola data dan state dari tampilan
    private val viewModel: BookViewModel by viewModels()

    // List untuk menyimpan daftar bab dalam buku
    private var currentChapters: List<Chapter> = emptyList()

    // Variabel untuk menyimpan isi buku yang sedang dibaca
    private lateinit var currentBookContent: BookContent

    // Variabel untuk menyimpan data buku yang sedang dibaca
    private lateinit var currentBook: Book

    // Launcher untuk meminta izin penyimpanan sebelum mengunduh buku dalam bentuk PDF
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Jika izin diberikan, mulai proses download buku sebagai PDF
            downloadBookAsPdf()
        } else {
            // Jika izin ditolak, tampilkan pesan peringatan kepada pengguna
            Toast.makeText(
                requireContext(),
                "Izin penyimpanan diperlukan untuk download buku",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookReaderBinding.bind(view)

        // Mengambil data buku dari argumen yang dikirim ke fragment Book Reader
        val args = BookReaderFragmentArgs.fromBundle(requireArguments())
        val bookId = args.bookContentArg.bookId
        val bookTitle = args.bookContentArg.title

        Timber.d("BookReaderFragment: Received book title: $bookTitle")


        // Menetapkan judul toolbar sesuai dengan judul buku
        binding.toolbar.title = bookTitle

        // Menambahkan menu toolbar untuk opsi download
        binding.toolbar.inflateMenu(R.menu.book_reader_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_download -> {
                    // Jika menu download dipilih, periksa izin penyimpanan
                    checkStoragePermissionAndDownload()
                    true
                }

                R.id.ratingFragment -> {
                    val action =
                        BookReaderFragmentDirections.actionBookReaderFragmentToRatingFragment(bookId)
                    findNavController().navigate(action)
                    true
                }

                else -> false
            }
        }

        // Menyiapkan spinner untuk memilih bab dalam buku
        setupChapterSpinner()
        // Mengamati perubahan data dalam ViewModel berdasarkan ID buku
        observeViewModel(bookId)
        // Menangani aksi tombol kembali
        setupBackButton()
    }

    private fun getCurrentBook(): BookData {
        return BookData(
            id = currentBook.id,
            title = currentBook.title,
            authorName = "",
            image = ""
        )
    }

    // Call this method when a chapter is selected in the spinner
    private fun onChapterSelected(position: Int) {
        if (currentChapters.isNotEmpty() && position < currentChapters.size) {
            val chapter = currentChapters[position]
            binding.contentTextView.text = chapter.content

            // Record reading history when chapter is selected
            val bookData = getCurrentBook()
            recordReadingHistory(bookData, chapter.number.toString())
        }
    }

    private fun recordReadingHistory(book: BookData, lastChapter: String) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email

        if (userId != null && userEmail != null) {
            db.collection("Books").document(book.id)
                .get()
                .addOnSuccessListener { document ->
                    // Debug information about the document
                    Timber.d("Book document exists: ${document.exists()}")
                    if (document.exists()) {
                        // Print all fields in the document to help debug
                        val data = document.data
                        Timber.d("Document data: $data")

                        // Check for various possible field names
                        val possibleTitleFields =
                            listOf("titleBook", "title", "name", "bookTitle", "book_title")
                        var foundTitle: String? = null

                        // Try each possible field name
                        for (field in possibleTitleFields) {
                            val value = document.getString(field)
                            Timber.d("Field '$field' value: $value")
                            if (!value.isNullOrEmpty()) {
                                foundTitle = value
                                Timber.d("Found title in field '$field': $foundTitle")
                                break
                            }
                        }

                        // Use the found title or fallback to book.title
                        val finalTitle = foundTitle ?: book.title
                        Timber.d("Final title being used: $finalTitle")

                        // Create a reading history entry
                        val historyId = db.collection("reading_history").document().id
                        val timestamp = System.currentTimeMillis()

                        val historyData = hashMapOf(
                            "id_history" to historyId,
                            "user_id" to userId,
                            "user_email" to userEmail,
                            "novel_id" to book.id,
                            "novel_title" to finalTitle,
                            "last_chapter" to lastChapter,
                            "last_read" to timestamp
                        )

                        // Log before save attempt
                        Timber.tag("ReadingHistory")
                            .d("Attempting to save reading history for $finalTitle, user: $userEmail")

                        // Save to Firestore
                        db.collection("reading_history")
                            .document(historyId)
                            .set(historyData)
                            .addOnSuccessListener {
                                Timber.tag("ReadingHistory")
                                    .d("History recorded successfully for $finalTitle")
                                // Also update the user's reading progress
                                updateReadingProgress(book.id, lastChapter)
                            }
                            .addOnFailureListener { e ->
                                Timber.tag("ReadingHistory")
                                    .e("Error recording reading history: ${e.message}")
                                // Try to give more detailed error information
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal mencatat riwayat: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Timber.d("Book document does not exist, using provided title")
                        saveHistoryWithTitle(book, lastChapter, book.title, userId, userEmail, db)
                    }
                }
                .addOnFailureListener { e ->
                    Timber.tag("ReadingHistory").e("Failed to fetch book title: ${e.message}")
                    saveHistoryWithTitle(book, lastChapter, book.title, userId, userEmail, db)
                }
        } else {
            Timber.tag("ReadingHistory").e("Cannot record history - user not logged in")
            Toast.makeText(
                requireContext(),
                "Anda harus login untuk mencatat kemajuan membaca",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Helper function to save history with a given title
    private fun saveHistoryWithTitle(
        book: BookData,
        lastChapter: String,
        titleToUse: String,
        userId: String,
        userEmail: String,
        db: FirebaseFirestore
    ) {
        val historyId = db.collection("reading_history").document().id
        val timestamp = System.currentTimeMillis()

        val historyData = hashMapOf(
            "id_history" to historyId,
            "user_id" to userId,
            "user_email" to userEmail,
            "novel_id" to book.id,
            "novel_title" to titleToUse,
            "last_chapter" to lastChapter,
            "last_read" to timestamp
        )

        db.collection("reading_history")
            .document(historyId)
            .set(historyData)
            .addOnSuccessListener {
                Timber.tag("ReadingHistory").d("History recorded with title: $titleToUse")
                updateReadingProgress(book.id, lastChapter)
            }
            .addOnFailureListener { err ->
                Timber.tag("ReadingHistory").e("Error with fallback recording: ${err.message}")
                Toast.makeText(
                    requireContext(),
                    "Gagal mencatat riwayat: ${err.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    // Add updateReadingProgress method to the fragment
    private fun updateReadingProgress(bookId: String, lastChapter: String) {
        val auth = FirebaseAuth.getInstance()   
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // First, get total chapters to calculate percentage
            db.collection("Books").document(bookId)
                .get()
                .addOnSuccessListener { bookDocument ->
                    val totalChapters = bookDocument.getLong("total_chapters")?.toDouble()
                        ?: currentChapters.size.toDouble()
                    val currentChapter = lastChapter.toDoubleOrNull() ?: 1.0

                    // Calculate reading percentage
                    val readingPercentage = (currentChapter / totalChapters) * 100.0

                    // Update the reading progress
                    val progressData = hashMapOf(
                        "book_id" to bookId,
                        "last_chapter" to lastChapter,
                        "percentage" to readingPercentage,
                        "last_updated" to System.currentTimeMillis()
                    )

                    db.collection("users").document(userId)
                        .collection("reading_progress").document(bookId)
                        .set(progressData)
                        .addOnSuccessListener {
                            Timber.tag("ReadingProgress").d("Progress updated: $readingPercentage%")
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("ReadingProgress")
                                .e("Failed to update progress: ${e.message}")
                        }
                }
        }
    }


    // Fungsi untuk mengatur spinner bab dalam buku
    private fun setupChapterSpinner() {
        binding.chapterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Jika ada bab yang tersedia, tampilkan isi dari bab yang dipilih
                    if (currentChapters.isNotEmpty() && position < currentChapters.size) {
                        binding.contentTextView.text = currentChapters[position].content
                    }
                    onChapterSelected(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    // Fungsi untuk mengamati perubahan data di ViewModel
    private fun observeViewModel(bookId: String) {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Menampilkan atau menyembunyikan progress bar saat data dimuat
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            // Jika terjadi error, tampilkan pesan error di layar
            if (errorMessage.isNotEmpty()) {
                binding.contentTextView.text = getString(R.string.error, errorMessage)
                Timber.e("Error loading book content: $errorMessage")
            }
        }

        viewModel.bookContent.observe(viewLifecycleOwner) { bookContentList ->
            if (bookContentList.isNotEmpty()) {
                // Ambil konten buku pertama dari daftar yang didapatkan
                val bookContent = bookContentList.first()
                currentBookContent = bookContent
                currentChapters = bookContent.chapters
                Timber.d("BookContent ID: ${bookContent.bookId}")
                Timber.d("BookContent Title: ${bookContent.title}")

                // Simpan data buku yang sedang dibaca
                currentBook = Book(
                    id = bookContent.bookId,
                    title = bookContent.title,
                )

                // Jika ada bab dalam buku, tampilkan daftar bab dalam spinner
                if (currentChapters.isNotEmpty()) {
                    val chapterTitles = currentChapters.map { "Bab ${it.number}: ${it.title}" }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        chapterTitles
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.chapterSpinner.adapter = adapter

                    // Tampilkan isi dari bab pertama sebagai default
                    binding.contentTextView.text = currentChapters.first().content
                } else {
                    // Jika buku tidak memiliki bab, tampilkan pesan kosong
                    binding.contentTextView.text = getString(R.string.empty_chapter)
                }
            } else {
                binding.contentTextView.text = getString(R.string.empty_book)
            }
        }

        Timber.d("Fetching book content for bookId: $bookId")
        // Memuat konten buku berdasarkan ID yang diberikan
        viewModel.loadBookContent(bookId)
    }

    // Fungsi untuk memeriksa izin penyimpanan sebelum mengunduh PDF
    private fun checkStoragePermissionAndDownload() {
        when {
            // Jika izin sudah diberikan, langsung mulai proses download
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                downloadBookAsPdf()
            }
            // Jika pengguna perlu diberikan penjelasan tambahan, tampilkan toast
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Toast.makeText(
                    requireContext(),
                    "Izin penyimpanan diperlukan untuk menyimpan buku",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            // Jika izin belum diberikan, minta izin kepada pengguna
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // Fungsi untuk mengunduh buku dalam format PDF
    private fun downloadBookAsPdf() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Nama file PDF akan dibuat berdasarkan judul buku
                val fileName = "${currentBook.title.replace(" ", "_")}.pdf"
                // Tentukan lokasi penyimpanan file PDF di penyimpanan eksternal
                val file = File(
                    requireContext().getExternalFilesDir(null),
                    fileName
                )

                // Membuat PDF
                PdfWriter(file).use { pdfWriter ->
                    val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
                    val document = Document(pdfDocument)

                    // Menambahkan Juduk pada hasil pdf
                    val titleText = Text(currentBook.title).setFontSize(20f)
                    document.add(Paragraph(titleText).setMarginBottom(20f))

                    // Menambahkan Bab
                    currentChapters.forEach { chapter ->
                        // Bab dan Judul
                        val chapterTitleText = Text("Bab ${chapter.number}: ${chapter.title}")
                            .setFontSize(16f)
                            .setBold()
                        document.add(Paragraph(chapterTitleText).setMarginTop(15f))

                        // Isi dari Bab
                        val chapterContentText = Text(chapter.content).setFontSize(12f)
                        document.add(Paragraph(chapterContentText))
                    }

                    document.close()
                }

                withContext(Dispatchers.Main) {
                    //Membuka file pdf setelah berhasil di download
                    openPdfFile(file)

                    Toast.makeText(
                        requireContext(),
                        "Buku berhasil diunduh di ${file.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengunduh buku: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    Timber.e(e, "PDF Download Error")
                }
            }
        }
    }

    // Fungsi untuk membuka file PDF menggunakan aplikasi pembaca PDF yang tersedia di perangkat.
    private fun openPdfFile(file: File) {
        try {
            // Mendapatkan URI dari file menggunakan FileProvider
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            // Membuat intent untuk membuka file PDF
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Tidak dapat membuka PDF",
                Toast.LENGTH_SHORT
            ).show()
            Timber.e(e, "Open PDF Error")
        }
    }

    //fungsi untuk mengatasi button kembali ke halaman sebelumnya
    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Menghapus referensi binding agar memori bisa dibebaskan
        _binding = null
    }
}