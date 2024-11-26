package com.okta.senov.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.okta.senov.databinding.ActivitySplashBinding // Import ViewBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding // Deklarasikan binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding dan setContentView
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set duration of splash screen (in milliseconds)
        val splashDuration = 3000L // 3 detik

        // Handler untuk mengarahkan ke MainActivity setelah splash screen selesai
        Handler().postDelayed({
            // Arahkan ke MainActivity setelah splash
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Menutup SplashActivity
        }, splashDuration)
    }
}
