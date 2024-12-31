package com.okta.senov.extensions

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation

fun View.findNavController(): NavController {
    return Navigation.findNavController(this)
}
