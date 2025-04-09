package com.okta.senov.activity

// Import library yang dibutuhkan
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.MyApplication
import com.okta.senov.R
import com.okta.senov.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

// Menandai class ini agar bisa menggunakan dependency injection dari Hilt
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Meng-inject class MyApplication untuk dependency injection
    @Inject
    lateinit var myApplication: MyApplication

    // Deklarasi view binding untuk mengakses elemen layout
    private lateinit var binding: ActivityMainBinding

    // Deklarasi navController untuk navigasi antar fragment
    private lateinit var navController: NavController

    // Firebase Auth untuk otentikasi pengguna
    private lateinit var auth: FirebaseAuth

    // Firestore untuk mengambil data user dan role-nya
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Agar ketika keyboard muncul, layout tidak terdorong (khusus untuk bottom navigation)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        // Inisialisasi Firebase Authentication dan Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Log debug, info, warning, dan error menggunakan Timber (untuk debugging)
        Timber.tag("MainActivity").d("Debug log: Activity created")
        Timber.tag("MainActivity").i("Info log: User ID = %s", "12345")
        Timber.tag("MainActivity").w("Warning log: Network error detected")
        Timber.tag("MainActivity").e(Throwable("Example error"), "Error log: Something went wrong")

        // Inisialisasi view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengambil NavHostFragment yang ada di layout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Menghubungkan BottomNavigationView dengan Navigation Controller
        binding.bottomNavigation.setupWithNavController(navController)

        // Mengecek role user (admin/user) dan mengatur halaman awal sesuai perannya
        checkUserRoleAndSetStartDestination()

        // Menyembunyikan atau menampilkan bottom navigation berdasarkan halaman tujuan
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.profileFragment,
                R.id.searchFragment,
                R.id.YourBookFragment -> {
                    // Bottom nav muncul di halaman-halaman ini
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                else -> {
                    // Bottom nav disembunyikan di halaman lainnya
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }
    }

    // Fungsi untuk mengecek role user (admin/user) dan mengatur halaman awal sesuai peran
    private fun checkUserRoleAndSetStartDestination() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .whereEqualTo("email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Jika data user ditemukan, ambil rolenya
                        val userDoc = documents.documents[0]
                        val role = userDoc.getString("role") ?: "user"

                        // Membuat ulang navGraph dan set halaman awal berdasarkan role
                        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                        when (role) {
                            "admin" -> {
                                // Jika admin, mulai dari adminFragment
                                navGraph.setStartDestination(R.id.adminFragment)
                                navController.graph = navGraph
                            }
                            else -> {
                                // Jika user biasa, mulai dari homeFragment
                                navGraph.setStartDestination(R.id.homeFragment)
                                navController.graph = navGraph
                            }
                        }
                    } else {
                        // Jika tidak ada data user, fallback ke homeFragment
                        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                        navGraph.setStartDestination(R.id.homeFragment)
                        navController.graph = navGraph
                    }
                }
                .addOnFailureListener {
                    // Jika gagal mengambil data user, fallback ke homeFragment
                    val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                    navGraph.setStartDestination(R.id.homeFragment)
                    navController.graph = navGraph
                }
        }
    }
}
