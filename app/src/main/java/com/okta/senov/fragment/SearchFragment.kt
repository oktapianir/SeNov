package com.okta.senov.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.okta.senov.R
import com.okta.senov.adapter.TopAuthorsAdapter
import com.okta.senov.adapter.TopBooksAdapter
import com.okta.senov.databinding.FragmentSearchBinding
import com.okta.senov.model.Author
import com.okta.senov.model.Book
import androidx.navigation.fragment.findNavController


class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var authorAdapter: TopAuthorsAdapter
    private lateinit var bookAdapter: TopBooksAdapter
    private lateinit var recentBookAdapter: TopBooksAdapter

    private val topAuthors = listOf(
        Author("Tere Liye", R.drawable.img_andrea_hirata),
        Author("Dewi Lestari", R.drawable.img_andrea_hirata),
        Author("Andrea Hirata", R.drawable.img_andrea_hirata),
        Author("Fiersa Besari", R.drawable.img_andrea_hirata),
        Author("Leila S.Chudori", R.drawable.img_andrea_hirata),
        Author("Pidi Baiq", R.drawable.img_andrea_hirata)
    )

    private val allBooks = listOf(
        Book(
            id = 1,
            title = "Bulan",
            author = "Tere Liye",
            genre = "Fantasi Petualang",
            rating = 4.5f,
            price = "$10.99",
            synopsis = "Kisah mengharukan tentang cinta dan pengorbanan dalam keluarga.",
            coverResourceId = R.drawable.img_book_cover1
        ),
        Book(
            id = 2,
            title = "Si Putih",
            author = "Tere Liye",
            genre = "Fantasi Petualang",
            rating = 4.7f,
            price = "$12.99",
            synopsis = "Kisah mendalam tentang kucing kesayangan Raib.",
            coverResourceId = R.drawable.img_book_cover2
        ),
        Book(
            id = 3,
            title = "Bumi",
            author = "Tere Liye",
            genre = "Fantasi Petualang",
            rating = 4.7f,
            price = "$12.99",
            synopsis = "Kisah mendalam tentang kucing kesayangan Raib.",
            coverResourceId = R.drawable.img_book_cover3
        ),
        Book(
            id = 4,
            title = "Ceroz Dan Batozar",
            author = "Tere Liye",
            genre = "Fantasi Petualang",
            rating = 4.7f,
            price = "$12.99",
            synopsis = "Kisah mendalam tentang kucing kesayangan Raib.",
            coverResourceId = R.drawable.img_book_cover6
        ),
    )

    private val recentBooks = listOf(
        Book(
            id = 5,
            title = "Selena",
            author = "Tere Liye",
            genre = "Drama",
            rating = 4.8f,
            price = "$15.99",
            synopsis = "Kisah tentang perjuangan anak-anak Belitung dalam mengejar impian.",
            coverResourceId = R.drawable.img_book_cover5
        )
    )

    private var displayedBooks = allBooks

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)

        // Setup for Authors RecyclerView
        binding.topAuthorsRecycler.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        authorAdapter = TopAuthorsAdapter(topAuthors)
        binding.topAuthorsRecycler.adapter = authorAdapter

        // Setup for Top Books RecyclerView
        binding.topBooksRecycler.layoutManager = LinearLayoutManager(context)
        bookAdapter = TopBooksAdapter(displayedBooks)
        binding.topBooksRecycler.adapter = bookAdapter

        // Setup for Recent Books RecyclerView
        binding.recentBooksRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recentBookAdapter = TopBooksAdapter(recentBooks)
        binding.recentBooksRecycler.adapter = recentBookAdapter

        // Back Button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
