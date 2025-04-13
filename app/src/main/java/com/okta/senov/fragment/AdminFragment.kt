package com.okta.senov.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAdminBinding
import com.okta.senov.extensions.findNavController
import timber.log.Timber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//import com.jjoe64.graphview.GraphView
//import com.jjoe64.graphview.helper.StaticLabelsFormatter
//import com.jjoe64.graphview.series.DataPoint
//import com.jjoe64.graphview.series.LineGraphSeries

class AdminFragment : Fragment(R.layout.fragment_admin) {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.tag("AdminFragment").d("onCreateView called")
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        binding.btnTambahAuthor.setOnClickListener {
            binding.btnTambahAuthor.findNavController()
                .navigate(R.id.action_adminFragment_to_addauthorFragment)
        }
        binding.btnTambahIsiContent.setOnClickListener {
            binding.btnTambahIsiContent.findNavController()
                .navigate(R.id.action_adminFragment_to_addBookFragment)
        }
        binding.btnAddBookContent.setOnClickListener {
            binding.btnAddBookContent.findNavController()
                .navigate(R.id.action_adminFragment_to_addContentBookFragment)
        }
        binding.btnDataRating.setOnClickListener {
            binding.btnDataRating.findNavController()
                .navigate(R.id.action_adminFragment_to_dataRatingFragment)
        }
        binding.cardLogout.setOnClickListener {
            logout()
        }
        //fungsi untuk mengambil jumlah keseluruhan data author
        fetchAuthorsCount()

        //fungsi untuk mengambil jumlah keseluran data buku
        fetchBooksCount()
        return binding.root
    }

    //    private fun logout() {
//        try {
//            // 1. Hapus token autentikasi
//            clearAuthToken()
//
//            // 2. Hapus data sesi sementara
//            clearTemporarySessionData()
//
//            // 3. Reset status login
//            resetLoginState()
//
//            // 4. Navigasi ke halaman login
//            navigateToLoginFragment()
//
//            // 5. Tampilkan pesan logout berhasil
//            showLogoutSuccessMessage()
//
//        } catch (e: Exception) {
//            Timber.e(e, "Logout failed")
//            showLogoutErrorMessage()
//        }
//    }
    private fun logout() {
        try {
            // 1. Sign out dari Firebase Authentication
            FirebaseAuth.getInstance().signOut()

            // 2. Hapus SEMUA data autentikasi dan profil pengguna
            val tokenPrefs =
                requireContext().getSharedPreferences("AuthTokenPrefs", Context.MODE_PRIVATE)
            tokenPrefs.edit().clear().apply()

            // 3. Hapus SEMUA data sesi
            val sessionPrefs =
                requireContext().getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE)
            sessionPrefs.edit().clear().apply()

            // 4. Reset status login sepenuhnya
            val loginStatePrefs =
                requireContext().getSharedPreferences("LoginState", Context.MODE_PRIVATE)
            loginStatePrefs.edit().clear().apply()

            // 5. Hapus data profil pengguna
            val userProfilePrefs =
                requireContext().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)
            userProfilePrefs.edit().clear().apply()

            // 6. Hapus cache pengguna lainnya jika ada
            // Tambahkan SharedPreferences lain yang mungkin menyimpan data user

            // 7. Navigasi ke fragment login
            navigateToLoginFragment()

            // 8. Tampilkan pesan berhasil
            showLogoutSuccessMessage()

        } catch (e: Exception) {
            Timber.e(e, "Logout failed")
            showLogoutErrorMessage()
        }
    }

    private fun navigateToLoginFragment() {
        // Navigasi dengan menghapus AdminFragment dari back stack
        binding.cardLogout.findNavController().navigate(
            R.id.action_adminFragment_to_loginFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.adminFragment, true)
                .build()
        )
    }

    private fun showLogoutSuccessMessage() {
        Toast.makeText(
            requireContext(),
            "Logout berhasil",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLogoutErrorMessage() {
        Toast.makeText(
            requireContext(),
            "Logout gagal. Silakan coba lagi.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun fetchAuthorsCount() {
        db.collection("authors")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val authorsCount = querySnapshot.size()
                binding.tvAuthorCount.text = authorsCount.toString()

                Timber.tag("AdminFragment").d("Total authors: $authorsCount")
            }
            .addOnFailureListener { exception ->
                Timber.tag("AdminFragment").e(exception, "Error fetching authors count")
                binding.tvAuthorCount.text = "0"
                Toast.makeText(
                    requireContext(),
                    "Gagal mengambil jumlah penulis",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchBooksCount() {
        db.collection("Books")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val booksCount = querySnapshot.size()
                binding.tvBookCount.text = booksCount.toString()

                Timber.tag("AdminFragment").d("Total books: $booksCount")
            }
            .addOnFailureListener { exception ->
                Timber.tag("AdminFragment").e(exception, "Error fetching books count")
                binding.tvBookCount.text = "0"
                Toast.makeText(
                    requireContext(),
                    "Gagal mengambil jumlah buku",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
//    private fun setupChart() {
//        // Buat GraphView
//        val graphView = GraphView(requireContext())
//        binding.chartContainer.removeAllViews()
//        binding.chartContainer.addView(graphView)
//
//        // Series untuk 2025
//        val series2025 = LineGraphSeries(arrayOf(
//            DataPoint(0.0, 10.0),
//            DataPoint(1.0, 15.0),
//            DataPoint(2.0, 12.0),
//            DataPoint(3.0, 18.0),
//            DataPoint(4.0, 22.0),
//            DataPoint(5.0, 17.0),
//            DataPoint(6.0, 19.0),
//            DataPoint(7.0, 23.0),
//            DataPoint(8.0, 20.0),
//            DataPoint(9.0, 25.0),
//            DataPoint(10.0, 19.0),
//            DataPoint(11.0, 24.0)
//        ))
//
//        // Series untuk 2024
//        val series2024 = LineGraphSeries(arrayOf(
//            DataPoint(0.0, 8.0),
//            DataPoint(1.0, 10.0),
//            DataPoint(2.0, 9.0),
//            DataPoint(3.0, 14.0),
//            DataPoint(4.0, 15.0),
//            DataPoint(5.0, 13.0),
//            DataPoint(6.0, 16.0),
//            DataPoint(7.0, 18.0),
//            DataPoint(8.0, 17.0),
//            DataPoint(9.0, 20.0),
//            DataPoint(10.0, 15.0),
//            DataPoint(11.0, 19.0)
//        ))
//
//        // Styling 2025
//        series2025.color = ContextCompat.getColor(requireContext(), R.color.lily)
//        series2025.thickness = 4
//
//        // Styling 2024
//        series2024.color = ContextCompat.getColor(requireContext(), R.color.white_72)
//        series2024.thickness = 4
//
//        // Tambahkan ke graph
//        graphView.addSeries(series2025)
//        graphView.addSeries(series2024)
//
//        // Styling graph
//        graphView.title = ""
//        graphView.titleTextSize = 0f
//        graphView.legendRenderer.isVisible = false
//
//        // X-axis labels
//        val monthLabels = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
//        val staticLabelsFormatter = StaticLabelsFormatter(graphView)
//        staticLabelsFormatter.setHorizontalLabels(monthLabels)
//        graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
//        // Viewport setting
//        graphView.viewport.isXAxisBoundsManual = true
//        graphView.viewport.setMinX(0.0)
//        graphView.viewport.setMaxX(11.0)
//
//        // Grid styling
//        graphView.gridLabelRenderer.gridColor = Color.LTGRAY
//        graphView.gridLabelRenderer.isHighlightZeroLines = false
//    }

    override fun onDestroyView() {
        Timber.tag("AdminFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }
}
