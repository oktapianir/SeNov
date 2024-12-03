package com.okta.senov.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okta.senov.R
import com.okta.senov.model.Book
import com.okta.senov.model.BookRepository
import com.okta.senov.data.BookDatabase
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookRepository: BookRepository
    private val _popularBooks = MutableLiveData<List<Book>>()
    val popularBooks: LiveData<List<Book>> = _popularBooks

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> = _allBooks

    init {
        val bookDao = BookDatabase.getDatabase(application).bookDao()
        bookRepository = BookRepository(bookDao)

        // Memasukkan buku ke database saat ViewModel dibuat
        insertBooks()

        // Mengambil data buku
        fetchBooks()
    }

    private fun fetchBooks() {
        viewModelScope.launch {
            // Mengambil semua buku dari repositori
            val allBooksList = bookRepository.getBooks()

            // Pisahkan buku berdasarkan rating
            val popular = allBooksList.filter { it.rating >= 4.5f }
            val all = allBooksList.filter { it.rating < 4.5f }

            // Update LiveData setelah data diproses
            _popularBooks.postValue(popular.ifEmpty { listOf() })
            _allBooks.postValue(all.ifEmpty { allBooksList }) // Menampilkan semua jika tidak ada popular
        }
    }

    private fun insertBooks() {
        viewModelScope.launch {
            val books = listOf(
                Book(
                    1, "Bulan", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99",
                    "   Petualangan Raib, Seli, dan Ali berlanjut ke dunia Klan Bulan. Mereka harus menghadapi pasukan Bayangan Hitam yang dipimpin Tamus. Konflik besar di dunia ini membuka rahasia kekuatan Raib, sekaligus menambah tantangan besar bagi persahabatan mereka.", R.drawable.img_book_cover1
                ),
                Book(
                    2, "Si Putih", "Tere Liye", "Fantasi Petualangan", 4.7f, "$10.99",
                    "\"Si Putih\" adalah kisah mendalam tentang kucing kesayangan Raib, yang ternyata memiliki peran besar dalam perjalanan serial Bumi. Buku ini mengungkapkan misteri tentang asal-usul Si Putih dan bagaimana ia terlibat dalam peristiwa besar di dunia paralel. Si Putih bukan hanya seekor kucing biasa; ia adalah makhluk yang penuh rahasia, dengan hubungan unik terhadap Raib dan dunia klan. Melalui sudut pandang Si Putih, pembaca dibawa menjelajahi dunia yang lebih luas dan mempelajari peran pentingnya dalam menjaga keseimbangan dunia.", R.drawable.img_book_cover2
                ),
                Book(
                    3, "Nebula", "Tere Liye", "Fantasi Petualangan", 4.0f, "$10.99",
                    "Petualangan Raib, Seli, dan Ali memasuki babak baru yang semakin menegangkan. Mereka dihadapkan pada konflik besar yang melibatkan semua klan dalam dunia paralel. Dalam buku ini, rahasia besar tentang kekuatan mereka dan hubungan antar klan mulai terungkap. Ancaman yang mereka hadapi lebih besar dari sebelumnya, memaksa ketiganya untuk membuat pilihan sulit yang akan menentukan masa depan semua dunia paralel. \\\"Nebula\\\" menjadi titik penting dalam cerita, dengan fokus pada perjuangan menjaga keseimbangan dan mengungkap identitas sebenarnya dari musuh mereka.", R.drawable.img_book_cover4
                ),
                Book(
                    4, "Selena", "Tere Liye", "Fantasi Petualangan", 4.3f, "$10.99",
                    "\"Buku ini menceritakan masa lalu Selena, mentor Raib yang bijaksana. Selena dulunya adalah seorang gadis biasa dari Klan Bulan yang memiliki rasa ingin tahu besar dan keberanian luar biasa. Cerita ini mengungkapkan bagaimana Selena terlibat dalam konflik besar antar klan dan perjalanan yang mengubah hidupnya. Melalui pengorbanan dan keputusan sulit, Selena menjadi sosok penting yang menjaga keseimbangan dunia paralel. Buku ini juga menjelaskan bagaimana Selena mendapatkan kekuatannya dan membangun hubungan dengan Si Tanpa Mahkota.", R.drawable.img_book_cover5
                ),
                Book(
                    5, "Ceroz & Batozar", "Tere Liye", "Fantasi Petualangan", 4.2f, "$10.99",
                    "Buku ini mengupas lebih dalam kisah Ceros dan Batozar, dua karakter misterius dalam dunia paralel. Ceros, seorang petarung yang kuat dan tangguh, dan Batozar, seorang mantan kriminal dengan masa lalu kelam, memiliki kisah yang saling terhubung dan penuh liku. Buku ini mengungkapkan rahasia besar tentang hubungan mereka dengan konflik di dunia klan, serta perjalanan mereka menuju penebusan dan pembuktian diri. Melalui petualangan ini, pembaca dibawa melihat sisi lain dari dunia paralel yang penuh bahaya, namun juga dipenuhi pelajaran berharga.", R.drawable.img_book_cover6
                ),
                Book(
                    6, "Bumi", "Tere Liye", "Fantasi Petualangan", 4.5f, "$10.99",
                    "Bumi adalah novel pembuka dari seri petualangan fantasi karya Tere Liye yang membawa pembaca ke dunia paralel yang penuh misteri, keajaiban, dan persahabatan. Cerita ini berpusat pada tokoh utama, Raib, seorang remaja biasa yang ternyata memiliki kemampuan luar biasa: ia dapat menghilang. Kemampuan ini telah dimilikinya sejak kecil, tetapi ia menyimpannya sebagai rahasia.", R.drawable.img_book_cover3
                )
            )

            bookRepository.insertBooks(books)
        }
    }
}
