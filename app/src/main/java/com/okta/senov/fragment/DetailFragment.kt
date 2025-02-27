//package com.okta.senov.fragment
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.activity.OnBackPressedCallback
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.bumptech.glide.Glide
//import com.okta.senov.R
//import com.okta.senov.databinding.FragmentDetailBinding
//import com.okta.senov.model.Author
//import com.okta.senov.model.Book
//import com.okta.senov.viewmodel.YourBookViewModel
//
//@Suppress("DEPRECATION")
//class DetailFragment : Fragment() {
//    private var _binding: FragmentDetailBinding? = null
//
//    //    private val binding get() = _binding!!
//    private lateinit var binding: FragmentDetailBinding
//    private val yourBookViewModel: YourBookViewModel by viewModels()
//    private val args: DetailFragmentArgs by navArgs()
//
//    private val bookDescriptions = mapOf(
//        "The Hunger Games" to "The Hunger Games menceritakan tentang seorang gadis berusia 16 tahun bernama Katniss Everdeen (Jennifer Lawrence) yang tinggal di sebuah tempat bernama Distrik 12, di mana mayoritas penduduknya bekerja sebagai penambang. Distrik 12 adalah sebuah distrik terakhir, salah satu dari 12 distrik di negara yang disebut Panem. Panem sendiri merupakan sebuah negara di mana dulunya Amerika Utara pernah berada, yang setelah bencana besar di Bumi menjadi satu-satunya wilayah yang selamat. Karena pemberontakan gagal terhadap pemerintahan Panem 75 tahun sebelumnya, pemerintahan Panem lantas melakukan sebuah kompetisi maut yang diadakan di ibu kota negara yang bernama Capitol.",
//        "Harry Potter and the deathly hallows" to "Harry Potter and the Philosopher's Stone karya J.K. Rowling menceritakan kisah seorang anak laki-laki bernama Harry Potter yang tinggal bersama keluarga Dursley yang kejam. Pada ulang tahunnya yang ke-11, Harry mengetahui bahwa dia adalah seorang penyihir dan diundang untuk belajar di sekolah sihir Hogwarts. Di sana, dia bertemu dengan teman-temannya, Ron Weasley dan Hermione Granger. Mereka menemukan bahwa sebuah batu bertuah, yang dikenal sebagai Batu Bertuah (Philosopher's Stone), dapat memberi keabadian. Harry dan teman-temannya harus melawan kekuatan jahat yang ingin mendapatkan batu itu, termasuk penyihir jahat, Lord Voldemort, yang telah mencoba membunuh Harry ketika dia masih bayi. Buku ini mengawali petualangan Harry di dunia sihir dan perjuangannya melawan kekuatan gelap.",
//        "Harry Potter and the Half-Blood Prince" to "Harry Potter and the Half-Blood Prince mengisahkan Harry yang kembali ke Hogwarts untuk tahun keenamnya. Dia mulai mempelajari masa lalu Lord Voldemort melalui ingatan yang diberikan oleh Profesor Dumbledore. Sementara itu, hubungan antar teman-teman semakin rumit, terutama dengan Ron dan Hermione. Harry menemukan sebuah buku milik \"Half-Blood Prince\" yang membantunya dalam pelajaran Sihir. Di sisi lain, Voldemort semakin kuat, dan perang melawan penyihir jahat tak terhindarkan. Di akhir cerita, tragedi besar terjadi dengan kematian Dumbledore, yang meninggalkan Harry dengan misi penting untuk menghancurkan Horcruxes, benda yang menyimpan bagian jiwa Voldemort.",
//        "Harry Potter and the Half-Blood Prince - Ravenclaw Edition" to "Harry Potter and the Half-Blood Prince tidak secara langsung berfokus pada Diadem Ravenclaw. Namun, dalam buku ini, Harry belajar lebih banyak tentang Horcruxes, objek yang menyimpan bagian jiwa Lord Voldemort, termasuk Diadem Ravenclaw. Diadem ini menjadi salah satu dari tujuh Horcrux yang harus dihancurkan untuk mengalahkan Voldemort. Sementara itu, cerita utamanya berkisar pada hubungan antara karakter-karakter utama, persiapan untuk perang melawan Voldemort, serta kematian tragis Dumbledore, yang memperburuk keadaan.",
//        "Harry Potter and the Half-Blood Prince - Gryffindor Edition" to "Harry Potter and the Half-Blood Prince tidak membahas Diadem Gryffindor secara langsung. Buku ini lebih fokus pada upaya Harry untuk mempelajari lebih banyak tentang masa lalu Lord Voldemort dan Horcruxes, termasuk Diadem Ravenclaw, yang menjadi salah satu objek yang menyimpan bagian jiwa Voldemort. Meskipun Diadem Gryffindor tidak disebutkan dalam buku ini, cerita utama berpusat pada hubungan antara karakter-karakter utama, pelajaran penting dari Dumbledore, dan pertempuran yang semakin dekat dengan kekuatan jahat Voldemort.",
//        "Harry Potter and the Half-Blood Prince - Hufflepuff Edition" to "Harry Potter and the Half-Blood Prince - Hufflepuff Edition mengikuti Harry di tahun keenamnya di Hogwarts, di mana ia menemukan buku ajaib milik \"Pangeran Berdarah Campuran\" dan mengungkap rahasia penting tentang Voldemort. Edisi ini menyoroti nilai-nilai Hufflepuff seperti kerja keras, kesetiaan, dan keadilan, dengan desain khas asrama Hufflepuff.",
//        "Harry Potter and the Order of the Phonenix" to "Harry Potter and the Order of the Phoenix mengisahkan Harry yang kembali ke Hogwarts untuk tahun kelima dan menghadapi penolakan terhadap ceritanya tentang kembalinya Lord Voldemort. Dia bergabung dengan sebuah kelompok rahasia bernama Order of the Phoenix yang bertujuan melawan Voldemort dan pengikutnya, Death Eaters. Di sekolah, Harry menghadapi tekanan dari pihak Kementerian Sihir dan pertikaian dengan Profesor Umbridge, yang ditunjuk sebagai pengawas dari kementerian. Di akhir cerita, Harry mengetahui tentang ramalan yang menghubungkannya dengan Voldemort, dan teman-temannya berjuang di Departemen Misteri, di mana mereka mengalami tragedi besar dengan kematian Sirius Black.",
//        "The Hobbit" to "The Hobbit karya J.R.R. Tolkien adalah kisah petualangan Bilbo Baggins, seorang hobbit yang menjalani kehidupan tenang hingga diajak oleh penyihir Gandalf untuk membantu sekelompok kurcaci merebut kembali harta mereka dari naga Smaug. Dalam perjalanan, Bilbo menghadapi berbagai rintangan, menemukan keberanian, dan memperoleh cincin ajaib yang memiliki kekuatan luar biasa. Cerita ini penuh dengan aksi, humor, dan pesan tentang keberanian serta persahabatan.",
//        "Catching Fire" to "Catching Fire adalah buku kedua dari seri The Hunger Games karya Suzanne Collins. Katniss Everdeen kembali ke arena mematikan dalam edisi khusus Quarter Quell setelah kemenangannya di Hunger Games sebelumnya. Di tengah ancaman Presiden Snow, Katniss menjadi simbol pemberontakan yang berkembang di seluruh Panem. Dengan aliansi baru dan tekanan bertahan hidup, ia harus menghadapi pengkhianatan, strategi politik, dan risiko yang lebih besar dari sebelumnya.",
//        "The Lightning Thief" to "The Lightning Thief adalah buku pertama dalam seri Percy Jackson & the Olympians karya Rick Riordan. Percy Jackson, seorang remaja yang baru mengetahui dirinya adalah setengah dewa, dituduh mencuri petir milik Zeus. Bersama teman-temannya, Annabeth dan Grover, Percy memulai petualangan untuk menemukan petir tersebut, membersihkan namanya, dan mencegah perang antar dewa. Dalam perjalanannya, Percy menghadapi berbagai monster dan mengungkap rahasia tentang dirinya dan keluarganya."
//    )
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentDetailBinding.bind(view)
//        val book = arguments?.getParcelable<Book>("bookArg") ?: args.book
//
//        // Tampilkan data buku di UI
//        binding.bookTitle.text = book.title
//        binding.authorName.text = book.author
//        binding.bookCoverImageView.setImageResource(book.coverResourceId)
//
//        // Ketika tombol "Read Now" diklik, tambahkan buku ke YourBookFragment
//        binding.listenButton.setOnClickListener {
//            yourBookViewModel.addBook(book)
//
//            // Navigasi ke YourBookFragment
//            findNavController().navigate(R.id.action_detail_to_yourbook)
//        }
//    }
//}
//
//override fun onCreateView(
//    inflater: LayoutInflater,
//    container: ViewGroup?,
//    savedInstanceState: Bundle?
//): View {
//    _binding = FragmentDetailBinding.inflate(inflater, container, false)
//
//    val book = arguments?.getParcelable<Book>("bookArg")
//
//    book?.let { setupBookDetails(it) }
//
//    setupBackButton()
//
//    return binding.root
//}
//
////private fun setupBookDetails(book: Book) {
////    binding.apply {
////        Glide.with(binding.bookCoverImageView.context)
////            .load(book.coverResourceId)
////            .into(binding.bookCoverImageView)
////        bookTitle.text = book.title
////        bookSubtitle.text = getString(R.string.karya)
////
////        val description = bookDescriptions[book.title] ?: "Deskripsi tidak tersedia"
////        bookDescriptionTextView.text = description
////    }
////}
//private fun setupBookDetails(book: Book, authors: List<Author>) {
//    binding.apply {
//        bookTitle.text = book.title
//        authorName.text = book.authorName // Tampilkan nama penulis
//
//        // Cari penulis berdasarkan nama
//        val author = authors.find { it.name == book.authorName }
//
//        // Tampilkan gambar author jika ada
//        author?.let {
//            Glide.with(binding.root)
//                .load(it.imageResId) // Load URL gambar dari API
//                .into(binding.authorImageView)
//        }
//    }
//}
//
//private fun setupBackButton() {
//    requireActivity().onBackPressedDispatcher.addCallback(
//        viewLifecycleOwner,
//        object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                requireActivity().supportFragmentManager.popBackStack()
//            }
//        }
//    )
//
//    binding.backButton.setOnClickListener {
//        requireActivity().supportFragmentManager.popBackStack()
//    }
//}
//
//override fun onDestroyView() {
//    super.onDestroyView()
//    _binding = null
//}
//

package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentDetailBinding
import com.okta.senov.model.Book
import com.okta.senov.viewmodel.YourBookViewModel
import timber.log.Timber

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private val yourBookViewModel: YourBookViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val book = args.bookArg
        setupBookDetails(book)

        // Tombol "Read Now"
        binding.listenButton.setOnClickListener {
            yourBookViewModel.addBook(book)
            findNavController().navigate(R.id.action_detail_to_yourbook)
        }

        setupBackButton()
    }

    private fun setupBookDetails(book: Book) {
        binding.apply {
            bookTitle.text = book.title
//            authorName.text = book.authorName

            Glide.with(bookCoverImageView.context)
                .load(book.coverResourceId)
                .into(bookCoverImageView)

            // Cek apakah deskripsi buku tersedia di map
            val description = bookDescriptions[book.title] ?: "Deskripsi tidak tersedia"
            bookDescriptionTextView.text = description
        }
        val bookId = "lRvC5g2AX1oPjH8E9qbo"
        fetchBookAuthor(bookId)

    }
    private fun fetchBookAuthor(bookId: String) {
        db.collection("authors").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val author = document.getString("nama") ?: "Tidak ada data"
                    binding.authorName.text = author
                    Timber.tag("Firestore").d("Data berhasil diambil: $author")
                } else {
                    Timber.tag("Firestore").e("Dokumen tidak ditemukan")
                    binding.authorName.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Timber.tag("FirestoreError").e("Gagal mengambil data: ${e.message}")
                binding.authorName.visibility = View.GONE
            }
    }

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
        _binding = null
    }

    private val bookDescriptions = mapOf(
        "The Hunger Games" to "The Hunger Games menceritakan tentang seorang gadis berusia 16 tahun bernama Katniss Everdeen (Jennifer Lawrence) yang tinggal di sebuah tempat bernama Distrik 12, di mana mayoritas penduduknya bekerja sebagai penambang. Distrik 12 adalah sebuah distrik terakhir, salah satu dari 12 distrik di negara yang disebut Panem. Panem sendiri merupakan sebuah negara di mana dulunya Amerika Utara pernah berada, yang setelah bencana besar di Bumi menjadi satu-satunya wilayah yang selamat. Karena pemberontakan gagal terhadap pemerintahan Panem 75 tahun sebelumnya, pemerintahan Panem lantas melakukan sebuah kompetisi maut yang diadakan di ibu kota negara yang bernama Capitol.",
        "Harry Potter and the deathly hallows" to "Harry Potter and the Philosopher's Stone karya J.K. Rowling menceritakan kisah seorang anak laki-laki bernama Harry Potter yang tinggal bersama keluarga Dursley yang kejam. Pada ulang tahunnya yang ke-11, Harry mengetahui bahwa dia adalah seorang penyihir dan diundang untuk belajar di sekolah sihir Hogwarts. Di sana, dia bertemu dengan teman-temannya, Ron Weasley dan Hermione Granger. Mereka menemukan bahwa sebuah batu bertuah, yang dikenal sebagai Batu Bertuah (Philosopher's Stone), dapat memberi keabadian. Harry dan teman-temannya harus melawan kekuatan jahat yang ingin mendapatkan batu itu, termasuk penyihir jahat, Lord Voldemort, yang telah mencoba membunuh Harry ketika dia masih bayi. Buku ini mengawali petualangan Harry di dunia sihir dan perjuangannya melawan kekuatan gelap.",
        "Harry Potter and the Half-Blood Prince" to "Harry Potter and the Half-Blood Prince mengisahkan Harry yang kembali ke Hogwarts untuk tahun keenamnya. Dia mulai mempelajari masa lalu Lord Voldemort melalui ingatan yang diberikan oleh Profesor Dumbledore. Sementara itu, hubungan antar teman-teman semakin rumit, terutama dengan Ron dan Hermione. Harry menemukan sebuah buku milik \"Half-Blood Prince\" yang membantunya dalam pelajaran Sihir. Di sisi lain, Voldemort semakin kuat, dan perang melawan penyihir jahat tak terhindarkan. Di akhir cerita, tragedi besar terjadi dengan kematian Dumbledore, yang meninggalkan Harry dengan misi penting untuk menghancurkan Horcruxes, benda yang menyimpan bagian jiwa Voldemort.",
        "Harry Potter and the Half-Blood Prince - Ravenclaw Edition" to "Harry Potter and the Half-Blood Prince tidak secara langsung berfokus pada Diadem Ravenclaw. Namun, dalam buku ini, Harry belajar lebih banyak tentang Horcruxes, objek yang menyimpan bagian jiwa Lord Voldemort, termasuk Diadem Ravenclaw. Diadem ini menjadi salah satu dari tujuh Horcrux yang harus dihancurkan untuk mengalahkan Voldemort. Sementara itu, cerita utamanya berkisar pada hubungan antara karakter-karakter utama, persiapan untuk perang melawan Voldemort, serta kematian tragis Dumbledore, yang memperburuk keadaan.",
        "Harry Potter and the Half-Blood Prince - Gryffindor Edition" to "Harry Potter and the Half-Blood Prince tidak membahas Diadem Gryffindor secara langsung. Buku ini lebih fokus pada upaya Harry untuk mempelajari lebih banyak tentang masa lalu Lord Voldemort dan Horcruxes, termasuk Diadem Ravenclaw, yang menjadi salah satu objek yang menyimpan bagian jiwa Voldemort. Meskipun Diadem Gryffindor tidak disebutkan dalam buku ini, cerita utama berpusat pada hubungan antara karakter-karakter utama, pelajaran penting dari Dumbledore, dan pertempuran yang semakin dekat dengan kekuatan jahat Voldemort.",
        "Harry Potter and the Half-Blood Prince - Hufflepuff Edition" to "Harry Potter and the Half-Blood Prince - Hufflepuff Edition mengikuti Harry di tahun keenamnya di Hogwarts, di mana ia menemukan buku ajaib milik \"Pangeran Berdarah Campuran\" dan mengungkap rahasia penting tentang Voldemort. Edisi ini menyoroti nilai-nilai Hufflepuff seperti kerja keras, kesetiaan, dan keadilan, dengan desain khas asrama Hufflepuff.",
        "Harry Potter and the Order of the Phonenix" to "Harry Potter and the Order of the Phoenix mengisahkan Harry yang kembali ke Hogwarts untuk tahun kelima dan menghadapi penolakan terhadap ceritanya tentang kembalinya Lord Voldemort. Dia bergabung dengan sebuah kelompok rahasia bernama Order of the Phoenix yang bertujuan melawan Voldemort dan pengikutnya, Death Eaters. Di sekolah, Harry menghadapi tekanan dari pihak Kementerian Sihir dan pertikaian dengan Profesor Umbridge, yang ditunjuk sebagai pengawas dari kementerian. Di akhir cerita, Harry mengetahui tentang ramalan yang menghubungkannya dengan Voldemort, dan teman-temannya berjuang di Departemen Misteri, di mana mereka mengalami tragedi besar dengan kematian Sirius Black.",
        "The Hobbit" to "The Hobbit karya J.R.R. Tolkien adalah kisah petualangan Bilbo Baggins, seorang hobbit yang menjalani kehidupan tenang hingga diajak oleh penyihir Gandalf untuk membantu sekelompok kurcaci merebut kembali harta mereka dari naga Smaug. Dalam perjalanan, Bilbo menghadapi berbagai rintangan, menemukan keberanian, dan memperoleh cincin ajaib yang memiliki kekuatan luar biasa. Cerita ini penuh dengan aksi, humor, dan pesan tentang keberanian serta persahabatan.",
        "Catching Fire" to "Catching Fire adalah buku kedua dari seri The Hunger Games karya Suzanne Collins. Katniss Everdeen kembali ke arena mematikan dalam edisi khusus Quarter Quell setelah kemenangannya di Hunger Games sebelumnya. Di tengah ancaman Presiden Snow, Katniss menjadi simbol pemberontakan yang berkembang di seluruh Panem. Dengan aliansi baru dan tekanan bertahan hidup, ia harus menghadapi pengkhianatan, strategi politik, dan risiko yang lebih besar dari sebelumnya.",
        "The Lightning Thief" to "The Lightning Thief adalah buku pertama dalam seri Percy Jackson & the Olympians karya Rick Riordan. Percy Jackson, seorang remaja yang baru mengetahui dirinya adalah setengah dewa, dituduh mencuri petir milik Zeus. Bersama teman-temannya, Annabeth dan Grover, Percy memulai petualangan untuk menemukan petir tersebut, membersihkan namanya, dan mencegah perang antar dewa. Dalam perjalanannya, Percy menghadapi berbagai monster dan mengungkap rahasia tentang dirinya dan keluarganya."
    )
}
