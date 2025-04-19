package com.okta.senov.fragment

import android.content.Intent
import com.okta.senov.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import com.okta.senov.databinding.FragmentLaporanBinding
import com.okta.senov.databinding.ItemLaporanDocBinding
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BookReport(
    val idBook: String,
    val titleBook: String,
    val nameAuthor: String,
    val nameCategory: String,
    val description: String,
    var totalRead: Int = 0,
    var bookmarkCount: Int = 0
)

data class CategoryReport(
    val idBook: String,
    val nameCategory: String,
    var bookCount: Int = 0,
    var totalRead: Int = 0
)

data class AuthorReport(
    val authorId: String,
    val authorName: String,
    var bookCount: Int = 0,
    var totalRead: Int = 0,
    var favoriteCount: Int = 0
)

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private val bookReports = mutableListOf<BookReport>()
    private val categoryReports = mutableListOf<CategoryReport>()
    private val authorReports = mutableListOf<AuthorReport>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        // Setup Back Button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup Export Button
        binding.btnExport.setOnClickListener {
            exportToPDF()
        }

        // Setup Sorting Spinner
        setupSpinner()

        // Setup Search Functionality
        setupSearch()

        // Setup TabLayout and ViewPager
        setupTabLayout()

        // Fetch initial data
        binding.loadingCard.visibility = View.VISIBLE
        fetchStatistics()
        fetchBookReports()
    }

    private fun setupSpinner() {
        val sortingOptions = arrayOf(
            "Total Dibaca Tertinggi",
            "Total Dibaca Terendah",
            "Abjad A-Z",
            "Abjad Z-A"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            sortingOptions
        )
        binding.sortingSpinner.setAdapter(adapter)

        binding.sortingSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> sortByReadCount(descending = true)
                1 -> sortByReadCount(descending = false)
                2 -> sortByTitle(descending = false)
                3 -> sortByTitle(descending = true)
            }
            displayReports()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                filterReports(query)
            }
        })
    }

    private fun filterReports(query: String) {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> {
                if (query.isEmpty()) {
                    displayReports(bookReports)
                } else {
                    val filteredList = bookReports.filter {
                        it.titleBook.lowercase().contains(query) ||
                                it.nameAuthor.lowercase().contains(query)
                    }
                    displayReports(filteredList)
                }
            }

            1 -> {
                if (query.isEmpty()) {
                    // Create a map from the categoryReports list
                    val tempMap = categoryReports.associateBy { it.nameCategory.lowercase() }
                    val emptyBooksMap = mutableMapOf<String, MutableList<String>>()
                    displayCategoryReports(tempMap, emptyBooksMap)
                } else {
                    val filteredList = categoryReports.filter {
                        it.nameCategory.lowercase().contains(query)
                    }
                    // Create a map from the filtered list
                    val tempMap = filteredList.associateBy { it.nameCategory.lowercase() }
                    val emptyBooksMap = mutableMapOf<String, MutableList<String>>()
                    displayCategoryReports(tempMap, emptyBooksMap)
                }
            }

            2 -> {
                if (query.isEmpty()) {
                    displayAuthorReports(authorReports)
                } else {
                    val filteredList = authorReports.filter {
                        it.authorName.lowercase().contains(query)
                    }
                    displayAuthorReports(filteredList)
                }
            }
        }
    }

    private fun setupTabLayout() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return DummyFragment()
            }
        }

        // Baru kemudian connect TabLayout dengan ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Data Buku"
                1 -> tab.text = "Kategori"
                2 -> tab.text = "Penulis"
            }
        }.attach()

        // Listener untuk perubahan tab
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.scrollView.visibility = View.VISIBLE
                when (tab.position) {
                    0 -> fetchBookReports()
                    1 -> fetchCategoryReports()
                    2 -> fetchAuthorReports()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // Fragment dummy untuk viewpager
    class DummyFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return View(context)
        }
    }

    private fun fetchStatistics() {
        db.collection("Books").get().addOnSuccessListener { snapshot ->
            binding.totalBooksTextView.text = snapshot.size().toString()
        }
        db.collection("Books").get().addOnSuccessListener { booksSnapshot ->
            val uniqueCategory = mutableSetOf<String>()

            // Log all category names to see what we're finding
            Timber.tag("LaporanFragment").d("Total books found: ${booksSnapshot.size()}")

            for (doc in booksSnapshot) {
                // Replace this line
                val categoryName = (doc.getString("nameCategory") ?: "").trim().lowercase()
                if (categoryName.isNotEmpty()) {
                    uniqueCategory.add(categoryName)
                }
            }

            // Log the final set of unique categories
            Timber.tag("LaporanFragment")
                .d("Unique categories (${uniqueCategory.size}): $uniqueCategory")

            binding.totalCategoriesTextView.text = uniqueCategory.size.toString()
        }

        // Hitung jumlah penulis yang unik
        db.collection("Books").get().addOnSuccessListener { booksSnapshot ->
            val uniqueAuthors = mutableSetOf<String>()
            for (doc in booksSnapshot) {
                val authorName = doc.getString("nameAuthor") ?: ""
                if (authorName.isNotEmpty()) {
                    uniqueAuthors.add(authorName)
                }
            }
            binding.totalPublishersTextView.text = uniqueAuthors.size.toString()
        }
    }

    private fun fetchBookReports() {
        binding.loadingCard.visibility = View.VISIBLE

        db.collection("Books").get().addOnSuccessListener { booksSnapshot ->
            val bookMap = mutableMapOf<String, BookReport>()

            for (doc in booksSnapshot) {
                val book = BookReport(
                    idBook = doc.getString("idBook") ?: doc.id,
                    titleBook = doc.getString("titleBook") ?: "",
                    nameAuthor = doc.getString("nameAuthor") ?: "",
                    nameCategory = doc.getString("nameCategory") ?: "",
                    description = doc.getString("bookDescription") ?: ""
                )
                bookMap[book.idBook] = book
            }

            // Ambil data reading_progress
            db.collection("data_read_listened").get().addOnSuccessListener { progressSnapshot ->
                for (doc in progressSnapshot) {
                    val bookId = doc.getString("bookId") ?: continue
                    // Get readCount value, default to 1 if not present
                    val readCount = doc.getLong("readCount")?.toInt() ?: 1
                    if (bookMap.containsKey(bookId)) {
                        bookMap[bookId]?.totalRead = (bookMap[bookId]?.totalRead ?: 0) + readCount
                    }
                }

                // Ambil data bookmarks jika diperlukan
                db.collection("bookmarks").get().addOnSuccessListener { bookmarksSnapshot ->
                    for (doc in bookmarksSnapshot) {
                        val bookId = doc.getString("book_id") ?: continue
                        if (bookMap.containsKey(bookId)) {
                            bookMap[bookId]?.bookmarkCount =
                                (bookMap[bookId]?.bookmarkCount ?: 0) + 1
                        }
                    }

                    // Update data untuk report
                    bookReports.clear()
                    bookReports.addAll(bookMap.values)

                    // Sort dan tampilkan
                    sortByReadCount(descending = true)
                    displayReports()

                    binding.loadingCard.visibility = View.GONE
                }
            }
        }.addOnFailureListener { e ->
            handleError("Gagal memuat data buku", e)
        }
    }

    private fun fetchCategoryReports() {
        binding.loadingCard.visibility = View.VISIBLE

        db.collection("Books").get().addOnSuccessListener { booksSnapshot ->
            // Create a map of category name -> category report
            val categoryMap = mutableMapOf<String, CategoryReport>()

            // Create a map of category name -> list of books in that category
            val categoryToBooks = mutableMapOf<String, MutableList<String>>()

            // First pass: Identify all unique categories and associate books
            for (doc in booksSnapshot) {
                val bookId = doc.getString("idBook") ?: doc.id
                val bookTitle = doc.getString("titleBook") ?: ""
                val categoryName = (doc.getString("nameCategory") ?: "").trim().lowercase()

                if (categoryName.isNotEmpty()) {
                    // Create category report if it doesn't exist
                    if (!categoryMap.containsKey(categoryName)) {
                        categoryMap[categoryName] = CategoryReport(
                            idBook = categoryName, // Using category name as ID
                            nameCategory = categoryName.capitalize(Locale.getDefault()),
                            bookCount = 0,
                            totalRead = 0
                        )
                        categoryToBooks[categoryName] = mutableListOf()
                    }

                    // Increment book count and add book to list
                    categoryMap[categoryName]?.bookCount =
                        (categoryMap[categoryName]?.bookCount ?: 0) + 1
                    categoryToBooks[categoryName]?.add(bookTitle)
                }
            }

            // Calculate total reads per category
            db.collection("reading_progress").get().addOnSuccessListener { progressSnapshot ->
                // Map of book ID to its category
                val bookToCategory = mutableMapOf<String, String>()

                // Create mapping of book IDs to categories
                for (doc in booksSnapshot) {
                    val bookId = doc.getString("idBook") ?: doc.id
                    val categoryName = (doc.getString("nameCategory") ?: "").trim().lowercase()
                    if (categoryName.isNotEmpty()) {
                        bookToCategory[bookId] = categoryName
                    }
                }

                // Count reads per category based on reading progress
                for (doc in progressSnapshot) {
                    val bookId = doc.getString("book_id") ?: doc.getString("novel_id") ?: continue
                    val categoryName = bookToCategory[bookId] ?: continue

                    if (categoryMap.containsKey(categoryName)) {
                        categoryMap[categoryName]?.totalRead =
                            (categoryMap[categoryName]?.totalRead ?: 0) + 1
                    }
                }

                // Update reports list
                categoryReports.clear()
                categoryReports.addAll(categoryMap.values)

                // Display reports with book lists
                displayCategoryReports(categoryMap, categoryToBooks)

                binding.loadingCard.visibility = View.GONE
            }
        }.addOnFailureListener { e ->
            handleError("Gagal memuat data kategori", e)
        }
    }

    private fun fetchAuthorReports() {
        binding.loadingCard.visibility = View.VISIBLE

        // Kumpulkan penulis dari buku
        db.collection("Books").get().addOnSuccessListener { booksSnapshot ->
            val authorMap = mutableMapOf<String, AuthorReport>()
            val bookToAuthorMap = mutableMapOf<String, String>()

            for (doc in booksSnapshot) {
                val authorName = doc.getString("nameAuthor") ?: ""
                val bookId = doc.getString("idBook") ?: doc.id

                if (authorName.isNotEmpty()) {
                    if (!authorMap.containsKey(authorName)) {
                        authorMap[authorName] = AuthorReport(
                            authorId = authorName,
                            authorName = authorName
                        )
                    }

                    // Tambah counter buku
                    authorMap[authorName]?.bookCount = (authorMap[authorName]?.bookCount ?: 0) + 1

                    // Simpan mapping buku ke penulis
                    bookToAuthorMap[bookId] = authorName
                }
            }

            // Hitung total dibaca per penulis
            db.collection("reading_progress").get().addOnSuccessListener { progressSnapshot ->
                for (doc in progressSnapshot) {
                    val bookId = doc.getString("book_id") ?: doc.getString("novel_id") ?: continue
                    val authorName = bookToAuthorMap[bookId] ?: continue

                    if (authorMap.containsKey(authorName)) {
                        authorMap[authorName]?.totalRead =
                            (authorMap[authorName]?.totalRead ?: 0) + 1
                    }
                }
                // Fetch favorite author data
                db.collection("favoriteAuthors").get().addOnSuccessListener { favoriteSnapshot ->
                    for (doc in favoriteSnapshot) {
                        val authorName = doc.getString("nameAuthor") ?: continue

                        if (authorMap.containsKey(authorName)) {
                            authorMap[authorName]?.favoriteCount =
                                (authorMap[authorName]?.favoriteCount ?: 0) + 1
                        }
                    }


                    // Update data untuk report
                    authorReports.clear()
                    authorReports.addAll(authorMap.values)

                    // Tampilkan
                    displayAuthorReports()

                    binding.loadingCard.visibility = View.GONE
                }
            }.addOnFailureListener { e ->
                handleError("Gagal memuat data penulis", e)
            }
        }
    }

        private fun sortByReadCount(descending: Boolean) {
            val sortedList = if (descending) {
                bookReports.sortedByDescending { it.totalRead }
            } else {
                bookReports.sortedBy { it.totalRead }
            }

            bookReports.clear()
            bookReports.addAll(sortedList)
        }

        private fun sortByTitle(descending: Boolean) {
            val sortedList = if (descending) {
                bookReports.sortedByDescending { it.titleBook }
            } else {
                bookReports.sortedBy { it.titleBook }
            }

            bookReports.clear()
            bookReports.addAll(sortedList)
        }

        private fun displayReports(reports: List<BookReport> = bookReports) {
            binding.reportContainer.removeAllViews()

            if (reports.isEmpty()) {
                binding.emptyStateView.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
            } else {
                binding.emptyStateView.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE

                for (report in reports) {
                    val itemBinding =
                        ItemLaporanDocBinding.inflate(
                            layoutInflater,
                            binding.reportContainer,
                            false
                        )
                    itemBinding.tvJudul.text = report.titleBook
                    itemBinding.tvPenulis.text = "Penulis: ${report.nameAuthor}"
                    itemBinding.tvCategory.text = report.nameCategory
                    itemBinding.tvTotalBaca.text = "Total Dibaca: ${report.totalRead}x"
                    itemBinding.tvDeskripsi.text = report.description
                    binding.reportContainer.addView(itemBinding.root)
                }
            }
        }

        private fun displayCategoryReports(
            categoryMap: Map<String, CategoryReport>,
            categoryToBooks: Map<String, MutableList<String>>
        ) {
            binding.reportContainer.removeAllViews()

            if (categoryMap.isEmpty()) {
                binding.emptyStateView.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
            } else {
                binding.emptyStateView.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE

                for ((categoryName, report) in categoryMap) {
                    val itemBinding = ItemLaporanDocBinding.inflate(
                        layoutInflater,
                        binding.reportContainer,
                        false
                    )

                    itemBinding.imgCover.setImageResource(R.drawable.ic_file)

                    // Set category name
                    itemBinding.tvJudul.text = report.nameCategory

                    // Set book count
                    itemBinding.tvPenulis.text = "Jumlah Buku: ${report.bookCount}"

                    // Set total read count
                    itemBinding.tvTotalBaca.text = "Total Dibaca: ${report.totalRead}"

                    itemBinding.tvTotalBaca.visibility = View.GONE
                    itemBinding.tvCategory.visibility = View.GONE
                    itemBinding.tvDeskripsi.visibility = View.GONE

                    // Add the books list (optional - if you want to show actual book titles)
                    val booksList = categoryToBooks[categoryName]
                    if (!booksList.isNullOrEmpty()) {
                        // You might need to add a TextView to your item layout for this
                        // Or create a nested LinearLayout with TextViews for each book
                        // For simplicity, I'll assume you'll add this to your layout

                        // Example using a StringBuilder to create a books list text
                        val booksText = StringBuilder("Buku: ")
                        booksList.forEachIndexed { index, bookTitle ->
                            if (index > 0) booksText.append(", ")
                            booksText.append(bookTitle)
                        }

                        // If you have a TextView for books list in your layout:
                        // itemBinding.tvBooksList.text = booksText.toString()
                    }

                    binding.reportContainer.addView(itemBinding.root)
                }
            }
        }

        private fun displayAuthorReports(reports: List<AuthorReport> = authorReports) {
            binding.reportContainer.removeAllViews()

            if (reports.isEmpty()) {
                binding.emptyStateView.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
            } else {
                binding.emptyStateView.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE

                for (report in reports) {
                    val itemBinding =
                        ItemLaporanDocBinding.inflate(
                            layoutInflater,
                            binding.reportContainer,
                            false
                        )
                    itemBinding.imgCover.setImageResource(R.drawable.ic_profile_placeholder)
                    itemBinding.tvJudul.text = report.authorName
                    itemBinding.tvPenulis.text = "Jumlah Buku: ${report.bookCount}"
                    itemBinding.tvTotalBaca.text = "Jumlah Favorit: ${report.favoriteCount}"

                    itemBinding.tvCategory.visibility = View.GONE
                    itemBinding.tvDeskripsi.visibility = View.GONE
                    binding.reportContainer.addView(itemBinding.root)
                }
            }
        }

        private fun exportToPDF() {
            when (binding.tabLayout.selectedTabPosition) {
                0 -> exportBooksToPDF()
                1 -> exportCategoriesToPDF()
                2 -> exportAuthorsToPDF()
            }
        }

        private fun exportBooksToPDF() {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filePath =
                File(requireContext().getExternalFilesDir(null), "Laporan_Buku_$timeStamp.pdf")

            try {
                val pdfWriter = PdfWriter(filePath)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                document.setMargins(36f, 36f, 36f, 36f)

                val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
                val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                val italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)

                // Header
                val headerParagraph = Paragraph("LAPORAN BUKU SEPUTAR NOVEL")
                    .setFont(boldFont)
                    .setFontSize(20f)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8f)
                document.add(headerParagraph)

                // Tanggal
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val currentDate = dateFormat.format(Date())
                val dateText = Paragraph("Tanggal: $currentDate")
                    .setFont(italicFont)
                    .setFontSize(11f)
                    .setTextAlignment(TextAlignment.RIGHT)
                document.add(dateText)

                // Statistik
                document.add(Paragraph("\n"))
                val statsTable =
                    Table(UnitValue.createPercentArray(floatArrayOf(33.33f, 33.33f, 33.33f)))
                        .setWidth(UnitValue.createPercentValue(100f))

                // Total Buku
                val totalBooksCell = Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(
                        Paragraph("Total Buku: ${binding.totalBooksTextView.text}")
                            .setFont(boldFont)
                            .setFontSize(12f)
                    )
                statsTable.addCell(totalBooksCell)
                // Total Categories
                val totalCategoriesCell = Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(
                        Paragraph("Total Kategori: ${binding.totalCategoriesTextView.text}")
                            .setFont(boldFont)
                            .setFontSize(12f)
                    )
                statsTable.addCell(totalCategoriesCell)

                // Total Publishers
                val totalPublishersCell = Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(
                        Paragraph("Total Penulis: ${binding.totalPublishersTextView.text}")
                            .setFont(boldFont)
                            .setFontSize(12f)
                    )
                statsTable.addCell(totalPublishersCell)

                document.add(statsTable)

                // Line separator
                val lineSeparator = LineSeparator(SolidLine(1f))
                document.add(Paragraph("\n"))
                document.add(lineSeparator)
                document.add(Paragraph("\n"))

                // Table header
                val table = Table(UnitValue.createPercentArray(floatArrayOf(40f, 30f, 30f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                table.addHeaderCell(
                    Cell()
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(Paragraph("Judul Buku"))
                )

                table.addHeaderCell(
                    Cell()
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(Paragraph("Penulis"))
                )

                table.addHeaderCell(
                    Cell()
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(Paragraph("Total Dibaca"))
                )

                // Table content
                for (report in bookReports) {
                    table.addCell(
                        Cell()
                            .setFont(regularFont)
                            .setFontSize(10f)
                            .add(Paragraph(report.titleBook))
                    )

                    table.addCell(
                        Cell()
                            .setFont(regularFont)
                            .setFontSize(10f)
                            .add(Paragraph(report.nameAuthor))
                    )

                    table.addCell(
                        Cell()
                            .setFont(regularFont)
                            .setFontSize(10f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(Paragraph(report.totalRead.toString()))
                    )
                }

                document.add(table)

                // Footer
                document.add(Paragraph("\n"))
                document.add(
                    Paragraph("Laporan ini dibuat otomatis oleh aplikasi Seputar Novel")
                        .setFont(italicFont)
                        .setFontSize(8f)
                        .setTextAlignment(TextAlignment.CENTER)
                )

                document.close()

                // open PDF file
                openPDF(filePath)

            } catch (e: Exception) {
                handleError("Gagal membuat PDF", e)
            }
        }

        private fun exportCategoriesToPDF() {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filePath =
                File(requireContext().getExternalFilesDir(null), "Laporan_Kategori_$timeStamp.pdf")

            try {
                // First, collect all book titles by category
                val categoryBooksMap = mutableMapOf<String, MutableList<String>>()

                // Populate the map with categories and their books
                db.collection("Books").get().addOnSuccessListener { booksSnapshot ->
                    for (doc in booksSnapshot) {
                        val bookTitle = doc.getString("titleBook") ?: ""
                        val categoryName = (doc.getString("nameCategory") ?: "").trim().lowercase()

                        if (categoryName.isNotEmpty() && bookTitle.isNotEmpty()) {
                            if (!categoryBooksMap.containsKey(categoryName)) {
                                categoryBooksMap[categoryName] = mutableListOf()
                            }
                            categoryBooksMap[categoryName]?.add(bookTitle)
                        }
                    }
                    try {
                    val pdfWriter = PdfWriter(filePath)
                    val pdfDocument = PdfDocument(pdfWriter)
                    val document = Document(pdfDocument)

                    document.setMargins(36f, 36f, 36f, 36f)

                    val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
                    val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                    val italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)

                    // Header
                    val headerParagraph = Paragraph("LAPORAN KATEGORI BUKU")
                        .setFont(boldFont)
                        .setFontSize(20f)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.BLUE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8f)
                    document.add(headerParagraph)

                    // Tanggal
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val currentDate = dateFormat.format(Date())
                    val dateText = Paragraph("Tanggal: $currentDate")
                        .setFont(italicFont)
                        .setFontSize(11f)
                        .setTextAlignment(TextAlignment.RIGHT)
                    document.add(dateText)

                    // Statistik
                    document.add(Paragraph("\n"))
                    val statsTable =
                        Table(UnitValue.createPercentArray(floatArrayOf(33.33f, 33.33f, 33.33f)))
                            .setWidth(UnitValue.createPercentValue(100f))

                    // Total Buku
                    val totalBooksCell = Cell()
                        .setBorder(Border.NO_BORDER)
                        .add(
                            Paragraph("Total Buku: ${binding.totalBooksTextView.text}")
                                .setFont(boldFont)
                                .setFontSize(12f)
                        )
                    statsTable.addCell(totalBooksCell)

                    // Total Categories
                    val totalCategoriesCell = Cell()
                        .setBorder(Border.NO_BORDER)
                        .add(
                            Paragraph("Total Kategori: ${binding.totalCategoriesTextView.text}")
                                .setFont(boldFont)
                                .setFontSize(12f)
                        )
                    statsTable.addCell(totalCategoriesCell)

                    // Total Publishers
                    val totalPublishersCell = Cell()
                        .setBorder(Border.NO_BORDER)
                        .add(
                            Paragraph("Total Penulis: ${binding.totalPublishersTextView.text}")
                                .setFont(boldFont)
                                .setFontSize(12f)
                        )
                    statsTable.addCell(totalPublishersCell)

                    document.add(statsTable)

                    // Line separator
                    val lineSeparator = LineSeparator(SolidLine(1f))
                    document.add(Paragraph("\n"))
                    document.add(lineSeparator)
                    document.add(Paragraph("\n"))

                    // Table header
                    val table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 25f, 25f)))
                        .setWidth(UnitValue.createPercentValue(100f))

                    table.addHeaderCell(
                        Cell()
                            .setFont(boldFont)
                            .setFontSize(12f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(Paragraph("Nama Kategori"))
                    )

                    table.addHeaderCell(
                        Cell()
                            .setFont(boldFont)
                            .setFontSize(12f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(Paragraph("Jumlah Buku"))
                    )

                    table.addHeaderCell(
                        Cell()
                            .setFont(boldFont)
                            .setFontSize(12f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(Paragraph("Judul Buku"))
                    )

                    // Table content
                    for (report in categoryReports) {
                        val categoryNameLowercase = report.nameCategory.lowercase()
                        val bookTitles = categoryBooksMap[categoryNameLowercase] ?: mutableListOf()

                        table.addCell(
                            Cell()
                                .setFont(regularFont)
                                .setFontSize(10f)
                                .add(Paragraph(report.nameCategory))
                        )

                        table.addCell(
                            Cell()
                                .setFont(regularFont)
                                .setFontSize(10f)
                                .setTextAlignment(TextAlignment.CENTER)
                                .add(Paragraph(report.bookCount.toString()))
                        )
                        // List all book titles for this category
                        val bookTitlesText = if (bookTitles.isEmpty()) {
                            "Tidak ada buku"
                        } else {
                            bookTitles.joinToString(", ")
                        }

                        table.addCell(
                            Cell()
                                .setFont(regularFont)
                                .setFontSize(10f)
                                .add(Paragraph(bookTitlesText))
                        )
                    }

                    document.add(table)

                    // Footer
                    document.add(Paragraph("\n"))
                    document.add(
                        Paragraph("Laporan ini dibuat otomatis oleh aplikasi SENOV")
                            .setFont(italicFont)
                            .setFontSize(8f)
                            .setTextAlignment(TextAlignment.CENTER)
                    )

                    document.close()

                    // Open PDF file
                    openPDF(filePath)

                    } catch (e: Exception) {
                        handleError("Gagal membuat PDF", e)
                    }
                }.addOnFailureListener { e ->
                    handleError("Gagal memuat data buku untuk laporan kategori", e)
                }
            } catch (e: Exception) {
                handleError("Gagal mempersiapkan laporan", e)
            }
        }

        private fun exportAuthorsToPDF() {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filePath =
                File(requireContext().getExternalFilesDir(null), "Laporan_Penulis_$timeStamp.pdf")

            try {
                val pdfWriter = PdfWriter(filePath)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                document.setMargins(36f, 36f, 36f, 36f)

                val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
                val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                val italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)

                // Header
                val headerParagraph = Paragraph("LAPORAN PENULIS BUKU")
                    .setFont(boldFont)
                    .setFontSize(20f)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8f)
                document.add(headerParagraph)

                // Tanggal
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val currentDate = dateFormat.format(Date())
                val dateText = Paragraph("Tanggal: $currentDate")
                    .setFont(italicFont)
                    .setFontSize(11f)
                    .setTextAlignment(TextAlignment.RIGHT)
                document.add(dateText)

                // Statistik
                document.add(Paragraph("\n"))
                val statsTable =
                    Table(UnitValue.createPercentArray(floatArrayOf(33.33f, 33.33f, 33.33f)))
                        .setWidth(UnitValue.createPercentValue(100f))

                // Total Buku
                val totalBooksCell = Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(
                        Paragraph("Total Buku: ${binding.totalBooksTextView.text}")
                            .setFont(boldFont)
                            .setFontSize(12f)
                    )
                statsTable.addCell(totalBooksCell)

                // Total Categories
                val totalCategoriesCell = Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(
                        Paragraph("Total Kategori: ${binding.totalCategoriesTextView.text}")
                            .setFont(boldFont)
                            .setFontSize(12f)
                    )
                statsTable.addCell(totalCategoriesCell)

                // Total Publishers
                val totalPublishersCell = Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(
                        Paragraph("Total Penulis: ${binding.totalPublishersTextView.text}")
                            .setFont(boldFont)
                            .setFontSize(12f)
                    )
                statsTable.addCell(totalPublishersCell)

                document.add(statsTable)

                // Line separator
                val lineSeparator = LineSeparator(SolidLine(1f))
                document.add(Paragraph("\n"))
                document.add(lineSeparator)
                document.add(Paragraph("\n"))

                // Table header
                val table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 25f, 25f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                table.addHeaderCell(
                    Cell()
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(Paragraph("Nama Penulis"))
                )

                table.addHeaderCell(
                    Cell()
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(Paragraph("Jumlah Buku"))
                )

                table.addHeaderCell(
                    Cell()
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(Paragraph("Jumlah Favorit"))
                )

                // Table content
                for (report in authorReports) {
                    table.addCell(
                        Cell()
                            .setFont(regularFont)
                            .setFontSize(10f)
                            .add(Paragraph(report.authorName))
                    )

                    table.addCell(
                        Cell()
                            .setFont(regularFont)
                            .setFontSize(10f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(Paragraph(report.bookCount.toString()))
                    )

                    table.addCell(
                        Cell()
                            .setFont(regularFont)
                            .setFontSize(10f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(Paragraph(report.favoriteCount.toString()))
                    )
                }

                document.add(table)

                // Footer
                document.add(Paragraph("\n"))
                document.add(
                    Paragraph("Laporan ini dibuat otomatis oleh aplikasi SENOV")
                        .setFont(italicFont)
                        .setFontSize(8f)
                        .setTextAlignment(TextAlignment.CENTER)
                )

                document.close()

                // Open PDF file
                openPDF(filePath)

            } catch (e: Exception) {
                handleError("Gagal membuat PDF", e)
            }
        }

        private fun openPDF(file: File) {
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

        private fun handleError(message: String, e: Exception) {
            Timber.e(e, message)
            Timber.tag("LaporanFragment").e(e, "$message: ${e.message}")
            binding.loadingCard.visibility = View.GONE
            Toast.makeText(requireContext(), "$message: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }