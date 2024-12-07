package com.okta.senov.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.okta.senov.databinding.ActivityMainBinding
import com.okta.senov.R

class MainActivity : AppCompatActivity() {

    // Declare the binding object
    private lateinit var binding: ActivityMainBinding
    // Declare navController as class property
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan NavController dari NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController // Assign to the class property

        // Menyambungkan BottomNavigation dengan NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Optional: menambahkan listener untuk navigasi manual jika diperlukan
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.nav_books -> {
                    if (navController.currentDestination?.id != R.id.booksNavGraph) {
                        navController.navigate(R.id.booksNavGraph)
                    }
                    true
                }
                R.id.nav_audio -> {
                    true
                }
                R.id.nav_profile -> {
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }

}
