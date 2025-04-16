package com.okta.senov.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAdminBinding
import com.okta.senov.extensions.findNavController
import timber.log.Timber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        binding.btnDataBantuanPengguna.setOnClickListener {
            binding.btnDataBantuanPengguna.findNavController()
                .navigate(R.id.action_adminFragment_to_dataBantuanPenggunaFragment)
        }
        binding.cardLogout.setOnClickListener {
            logout()
        }
        //fungsi untuk mengambil jumlah keseluruhan data author
        fetchAuthorsCount()

        //fungsi untuk mengambil jumlah keseluran data buku
        fetchBooksCount()
        setupChart()
        updateAverageValues()
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

    private fun setupChart() {
        val db = Firebase.firestore
        val chart: LineChart = binding.lineChart

        // Konfigurasi dasar chart sebelum data dimuat
        configureChartAppearance(chart)

        db.collection("reading_history")
            .get()
            .addOnSuccessListener { result ->
                val dateCountMap = mutableMapOf<String, Int>()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displaySdf =
                    SimpleDateFormat("dd MMM", Locale.getDefault()) // Format pendek untuk label

                for (doc in result) {
                    val lastReadMillis = doc.getLong("last_read") ?: continue
                    val date = Date(lastReadMillis)
                    val formattedDate = sdf.format(date)
                    dateCountMap[formattedDate] = dateCountMap.getOrDefault(formattedDate, 0) + 1
                }

                // Urutkan tanggal dan batasi jumlah entri yang ditampilkan jika terlalu banyak
                val sortedDates = dateCountMap.keys.sorted()
                val displayDates = if (sortedDates.size > 7) {
                    // Jika data terlalu banyak, cukup tampilkan 7 hari terakhir
                    sortedDates.takeLast(7)
                } else {
                    sortedDates
                }

                val entries = displayDates.mapIndexed { index, tanggal ->
                    Entry(index.toFloat(), dateCountMap[tanggal]?.toFloat() ?: 0f)
                }

                // Format tanggal menjadi lebih pendek untuk label
                val labels = displayDates.map {
                    displaySdf.format(sdf.parse(it) ?: Date())
                }

                // Buat dataset dengan styling menarik
                val dataSet = createBeautifulDataSet(entries)

                val lineData = LineData(dataSet)
                chart.data = lineData

                // Format X-Axis dengan label yang lebih pendek
                configureXAxis(chart.xAxis, labels)

                // Tambahkan highlight ke titik data saat disentuh
                chart.setOnChartValueSelectedListener(createChartValueListener())

                // Animasi saat memuat data
                chart.animateY(1200, Easing.EaseOutCubic)
                chart.invalidate()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configureChartAppearance(chart: LineChart) {
        // Warna background dan styling dasar
        chart.setBackgroundColor(Color.WHITE)
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)

        // Konfigurasi legend
        chart.legend.apply {
            textSize = 12f
            textColor = ContextCompat.getColor(requireContext(), R.color.black)
            form = Legend.LegendForm.CIRCLE
            formSize = 10f
            formLineWidth = 2f
            xEntrySpace = 10f
            isEnabled = true
        }

        // Konfigurasi interaksi
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        // Konfigurasi axis Y kiri (utama)
        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = ContextCompat.getColor(requireContext(), R.color.white_72)
            textColor = ContextCompat.getColor(requireContext(), R.color.black)
            textSize = 12f
            axisLineColor = Color.TRANSPARENT
            setDrawAxisLine(false)
            granularity = 1f  // Hanya nilai bulat
        }

        // Nonaktifkan axis Y kanan
        chart.axisRight.isEnabled = false

        // Hapus padding kiri/kanan agar grafik menggunakan seluruh ruang
        chart.setExtraOffsets(10f, 10f, 10f, 20f)
    }

    private fun configureXAxis(xAxis: XAxis, labels: List<String>) {
        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = 0f  // Horizontal labels
            setDrawGridLines(false)
            textSize = 12f
            textColor = ContextCompat.getColor(requireContext(), R.color.black)
            axisLineColor = Color.TRANSPARENT
            setDrawAxisLine(false)

            // Pastikan semua label terlihat
            labelCount = labels.size

            // Jika masih tumpang tindih, gunakan spacing
            if (labels.size > 5) {
                spaceMin = 0.5f
                spaceMax = 0.5f
            }
        }
    }

    private fun createBeautifulDataSet(entries: List<Entry>): LineDataSet {
        return LineDataSet(entries, "Jumlah Pembaca").apply {
            // Warna utama dengan color gradient effect
            val startColor = ContextCompat.getColor(requireContext(), R.color.lily)
            val endColor = ContextCompat.getColor(requireContext(), R.color.lily)

            // Line styling
            color = startColor
            lineWidth = 3f

            // Gradient fill
            setDrawFilled(true)
            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(ColorUtils.setAlphaComponent(startColor, 150), Color.TRANSPARENT)
            )

            // Circle styling
            setCircleColor(startColor)
            circleRadius = 5f
            circleHoleRadius = 2.5f
            setCircleHoleColor(Color.WHITE)

            // Value text styling
            setDrawValues(false) // Hide values initially, show on selection
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)

            // Highlight styling
            highLightColor = ContextCompat.getColor(requireContext(), R.color.lily)
            setDrawHighlightIndicators(true)
            highlightLineWidth = 1.5f
            enableDashedHighlightLine(10f, 5f, 0f)

            // Smoothness
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f // Less curvy
        }
    }

    private fun createChartValueListener(): OnChartValueSelectedListener {
        return object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val dataSet = binding.lineChart.data.getDataSetByIndex(
                        h?.dataSetIndex ?: 0
                    ) as LineDataSet

                    // Temporarily show the value for the selected point
                    dataSet.setDrawValues(true)
                    binding.lineChart.invalidate()

                    // Display additional info in a tooltip or text view
                    val value = it.y.toInt()
                    val dateIndex = it.x.toInt()
                    val dateLabel = binding.lineChart.xAxis.valueFormatter
                        .getFormattedValue(it.x, binding.lineChart.xAxis)

                    Toast.makeText(
                        requireContext(),
                        "$dateLabel: $value pembaca",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected() {
                // Hide values again when no point is selected
                val dataSet = binding.lineChart.data.getDataSetByIndex(0) as LineDataSet
                dataSet.setDrawValues(false)
                binding.lineChart.invalidate()
            }
        }
    }


    private val handler = Handler(Looper.getMainLooper())

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Bersihkan handler
    }


    private fun updateAverageValues() {
        val db = Firebase.firestore
        val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        db.collection("reading_history")
            .get()
            .addOnSuccessListener { result ->
                val dailyMap = mutableMapOf<String, Int>()
                val monthlyMap = mutableMapOf<String, Int>()

                for (doc in result) {
                    val lastRead = doc.getLong("last_read") ?: continue
                    val date = Date(lastRead)
                    val dayKey = sdfDay.format(date)
                    val monthKey = sdfMonth.format(date)

                    // Hitung pembaca harian
                    dailyMap[dayKey] = dailyMap.getOrDefault(dayKey, 0) + 1

                    // Hitung pembaca bulanan
                    monthlyMap[monthKey] = monthlyMap.getOrDefault(monthKey, 0) + 1
                }

                val dailyTotal = dailyMap.values.sum()
                val monthlyTotal = monthlyMap.values.sum()

                val dailyAverage =
                    if (dailyMap.isNotEmpty()) dailyTotal.toFloat() / dailyMap.size else 0f
                val monthlyAverage =
                    if (monthlyMap.isNotEmpty()) monthlyTotal.toFloat() / monthlyMap.size else 0f

                // Tambahkan efek angka berjalan saat menampilkan rata-rata
                animateTextView(0f, dailyAverage, binding.tvDailyAvg)
                animateTextView(0f, monthlyAverage, binding.tvMonthlyAvg)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menghitung rata-rata", Toast.LENGTH_SHORT)
                    .show()
            }

    }

    private fun animateTextView(initialValue: Float, finalValue: Float, textView: TextView) {
        val valueAnimator = ValueAnimator.ofFloat(initialValue, finalValue)
        valueAnimator.duration = 1500
        valueAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            textView.text = String.format("%.1f", animatedValue)
        }
        valueAnimator.start()
    }


    override fun onDestroyView() {
        Timber.tag("AdminFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }
}
