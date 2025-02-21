package com.okta.senov.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.tag("MainActivity").d("Debug log: Activity created")
        Timber.tag("MainActivity").i("Info log: User ID = %s", "12345")
        Timber.tag("MainActivity").w("Warning log: Network error detected")
        Timber.tag("MainActivity").e(Throwable("Example error"), "Error log: Something went wrong")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

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
}
