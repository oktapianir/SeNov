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
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.okta.senov.R
import com.okta.senov.databinding.FragmentBookReaderBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
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
                    findNavController().navigate(R.id.ratingFragment)
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

    // Fungsi untuk mengatur spinner bab dalam buku
    private fun setupChapterSpinner() {
        binding.chapterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Jika ada bab yang tersedia, tampilkan isi dari bab yang dipilih
                if (currentChapters.isNotEmpty() && position < currentChapters.size) {
                    binding.contentTextView.text = currentChapters[position].content
                }
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