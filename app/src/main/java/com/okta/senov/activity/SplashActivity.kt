package com.okta.senov.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.okta.senov.databinding.ActivitySplashBinding // Import ViewBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    // Deklarasi binding untuk mengakses elemen layout dengan aman
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menghubungkan layout XML ke activity menggunakan ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Durasi splash screen dalam milidetik (3000ms = 3 detik)
        val splashDuration = 3000L

        // Menjalankan MainActivity setelah delay 3 detik
        Handler(mainLooper).postDelayed({
            // Intent untuk berpindah dari SplashActivity ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Menutup SplashActivity agar tidak kembali ke splash saat tombol back ditekan
            finish()
        }, splashDuration)
    }
}
