package com.okta.senov.activity

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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var myApplication: MyApplication
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //for bottom navigation stay
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        //Inisialisasi firebase auth dna firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Timber.tag("MainActivity").d("Debug log: Activity created")
        Timber.tag("MainActivity").i("Info log: User ID = %s", "12345")
        Timber.tag("MainActivity").w("Warning log: Network error detected")
        Timber.tag("MainActivity").e(Throwable("Example error"), "Error log: Something went wrong")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        //cek role atau peran dari akun pengguna
        checkUserRoleAndSetStartDestination()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }

                R.id.profileFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }

                R.id.searchFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }

                R.id.YourBookFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }

                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }
    }

    private fun checkUserRoleAndSetStartDestination() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .whereEqualTo("email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.documents[0]
                        val role = userDoc.getString("role") ?: "user"

                        // Ubah start destination berdasarkan peran
                        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                        when (role) {
                            "admin" -> {
                                navGraph.setStartDestination(R.id.adminFragment)
                                navController.graph = navGraph
                            }
                            else -> {
                                navGraph.setStartDestination(R.id.homeFragment)
                                navController.graph = navGraph
                            }
                        }
                    } else {
                        // Jika tidak ada dokumen pengguna, gunakan homeFragment
                        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                        navGraph.setStartDestination(R.id.homeFragment)
                        navController.graph = navGraph
                    }
                }
                .addOnFailureListener {
                    // Fallback ke homeFragment jika ada kesalahan
                    val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                    navGraph.setStartDestination(R.id.homeFragment)
                    navController.graph = navGraph
                }
        }
    }

}

