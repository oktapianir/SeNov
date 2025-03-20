package com.okta.senov

import android.content.Context

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("SenovPrefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        private const val IS_LOGGED_IN = "IsLoggedIn"
        private const val USER_ROLE = "UserRole"
        private const val USER_EMAIL = "UserEmail"
    }

    fun createLoginSession(email: String, role: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ROLE, role)
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }

    fun getUserRole(): String {
        return sharedPreferences.getString(USER_ROLE, "user") ?: "user"
    }

    fun logoutUser() {
        editor.clear()
        editor.apply()
    }
}