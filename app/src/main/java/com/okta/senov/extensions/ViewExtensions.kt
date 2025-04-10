package com.okta.senov.extensions

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
//// Extension function untuk View agar bisa langsung mendapatkan NavController
fun View.findNavController(): NavController {
    // Menggunakan Navigation.findNavController dari AndroidX
    // untuk mendapatkan NavController yang terkait dengan View ini
    return Navigation.findNavController(this)
}
