package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.R
import com.okta.senov.databinding.FragmentLoginBinding
import com.okta.senov.viewmodel.LoginState
import com.okta.senov.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.tag("LoginFragment").d("onCreateView called")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        setupFirebaseAuth()
        setupClickListeners()
        observeLoginState()
        return binding.root
    }

    private fun setupFirebaseAuth() {
        Timber.tag("LoginFragment").d("Setting up Firebase Auth")
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun setupClickListeners() {
        Timber.tag("LoginFragment").d("Setting up click listeners")
        binding.apply {
            googleSignInButton.setOnClickListener {
                Timber.tag("LoginFragment").d("Google sign-in button clicked")
                signInWithGoogle()
            }
            icBack.setOnClickListener {
                Timber.tag("LoginFragment").d("Back button clicked")
                findNavController().navigate(R.id.action_loginFragment_to_fragmentProfile)
            }
            registerButton.setOnClickListener {
                Timber.tag("LoginFragment").d("Register button clicked")
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            loginButton.setOnClickListener {
                Timber.tag("LoginFragment").d("Login button clicked")
                handleNormalLogin()
            }
        }
    }

//    private fun handleNormalLogin() {
//        try {
//            val username = binding.emailEditText.text.toString()
//            val password = binding.passwordEditText.text.toString()
//
//            if (username.isEmpty() || password.isEmpty()) {
//                Timber.tag("LoginFragment").w("Empty username or password")
//                showSimpleToastMessage(R.string.error_empty_fields)
//                return
//            }
//
//            Timber.tag("LoginFragment").d("Attempting login with username: $username")
//            loginViewModel.login(username, password)
//        } catch (e: Exception) {
//            Timber.tag("LoginFragment").e(e, "Error during normal login")
//            Toast.makeText(
//                requireContext(),
//                "Terjadi kesalahan: ${e.message}",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }

    private fun handleNormalLogin() {
        try {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Timber.tag("LoginFragment").w("Empty email or password")
                showSimpleToastMessage(R.string.error_empty_fields)
                return
            }

            Timber.tag("LoginFragment").d("Attempting login with email: $email")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkUserRoleAndNavigate(email)
                    } else {
                        Timber.tag("LoginFragment").e("Login failed")
                        showSimpleToastMessage(R.string.error_authentication_failed)
                    }
                }
        } catch (e: Exception) {
            Timber.tag("LoginFragment").e(e, "Error during normal login")
            Toast.makeText(
                requireContext(),
                "Terjadi kesalahan: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loginViewModel.loginState.collect { state ->
                    when (state) {
                        is LoginState.Idle -> {
                            Timber.tag("LoginFragment").i("Login state: Idle")
                            binding.loginButton.isEnabled = true
                            binding.progressBar.isVisible = true
                        }
                        is LoginState.Loading -> {
                            Timber.tag("LoginFragment").d("Login state: Loading")
                            binding.loginButton.isEnabled = false
                            binding.progressBar.isVisible = true
                        }
                        is LoginState.Success -> {
                            Timber.tag("LoginFragment").i("Login successful")
                            binding.loginButton.isEnabled = true
                            binding.progressBar.isVisible = true
                            showSimpleToastMessage(R.string.login_success)
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                        is LoginState.Error -> {
                            Timber.tag("LoginFragment").e("Login error: ${state.message}")
                            binding.loginButton.isEnabled = true
                            binding.progressBar.isVisible = true
                            showSimpleToastMessage(R.string.error_authentication_failed)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag("LoginFragment").e(e, "Error observing login state")
                binding.progressBar.isVisible = true
                binding.loginButton.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Terjadi kesalahan: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun signInWithGoogle() {
        Timber.tag("LoginFragment").d("Initiating Google sign-in")
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInResultLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Timber.tag("LoginFragment").d("Google sign-in successful: ${account.email}")
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Timber.tag("LoginFragment").w("Google sign-in failed: ${e.statusCode}")
            showSimpleToastMessage(R.string.error_google_sign_in_failed)
            Timber.tag("LoginFragment").e(e, "Google sign-in error")
        }
    }

//    private fun firebaseAuthWithGoogle(idToken: String) {
//        Timber.tag("LoginFragment").d("Authenticating with Firebase using Google ID token")
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(requireActivity()) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Timber.tag("LoginFragment").i("Firebase auth successful: ${user?.displayName}")
//                    showToastMessage(R.string.login_success_with_name, user?.displayName ?: "")
//                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
//                } else {
//                    Timber.tag("LoginFragment").e("Firebase auth failed")
//                    showSimpleToastMessage(R.string.error_authentication_failed)
//                }
//            }
//    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Timber.tag("LoginFragment").d("Authenticating with Firebase using Google ID token")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Timber.tag("LoginFragment").i("Firebase auth successful: ${user?.displayName}")

                    // Periksa apakah pengguna adalah admin
                    if (user?.email == "admin@example.com") {
                        showSimpleToastMessage(R.string.login_success_admin)
                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                    } else {
                        showSimpleToastMessage(R.string.login_success_with_name)
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    Timber.tag("LoginFragment").e("Firebase auth failed")
                    showSimpleToastMessage(R.string.error_authentication_failed)
                }
            }
    }

    private fun showSimpleToastMessage(messageResId: Int) {
        Toast.makeText(
            requireContext(),
            getString(messageResId),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showToastMessage(messageResId: Int, vararg formatArgs: Any?) {
        Toast.makeText(
            requireContext(),
            getString(messageResId, *formatArgs),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        Timber.tag("LoginFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }

//    private fun checkUserRoleAndNavigate(email: String?) {
//        if (email == null) {
//            showSimpleToastMessage(R.string.error_authentication_failed)
//            return
//        }
//
//        // Periksa role di Firestore
//        val db = FirebaseFirestore.getInstance()
//        db.collection("users")
//            .whereEqualTo("email", email)
//            .get()
//            .addOnSuccessListener { documents ->
//                if (!documents.isEmpty) {
//                    val userDoc = documents.documents[0]
//                    when (userDoc.getString("role")) {
//                        "ADMIN" -> {
//                            Timber.tag("LoginFragment").d("User is admin: $email")
//                            showSimpleToastMessage(R.string.login_success_admin)
//                            findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
//                        }
//                        else -> {
//                            Timber.tag("LoginFragment").d("User is regular user: $email")
//                            showSimpleToastMessage(R.string.login_success_with_name)
//                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
//                        }
//                    }
//                } else {
//                    // User belum memiliki data di Firestore, buat sebagai user biasa
//                    createNewUserInFirestore(email, "USER")
//                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
//                }
//            }
//            .addOnFailureListener { e ->
//                Timber.tag("LoginFragment").e(e, "Error checking user role")
//                showSimpleToastMessage(R.string.error_authentication_failed)
//            }
//    }

    private fun checkUserRoleAndNavigate(email: String?) {
        if (email == null) {
            showSimpleToastMessage(R.string.error_authentication_failed)
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    val role = userDoc.getString("role") ?: "user" // Default ke "user" jika role tidak ditemukan

                    if (role == "admin") {
                        Timber.tag("LoginFragment").i("Admin login detected")
                        showSimpleToastMessage(R.string.login_success_admin)
                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment)
                    } else {
                        Timber.tag("LoginFragment").i("User login detected")
                        showSimpleToastMessage(R.string.login_success_with_name)
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    Timber.tag("LoginFragment").w("No user document found, defaulting to user")
                    showSimpleToastMessage(R.string.login_success_with_name)
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
            }
            .addOnFailureListener { e ->
                Timber.tag("LoginFragment").e(e, "Error fetching user role")
                showSimpleToastMessage(R.string.error_authentication_failed)
            }
    }


    private fun createNewUserInFirestore(email: String, role: String) {
        val db = FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "email" to email,
            "role" to role,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document()
            .set(userData)
            .addOnSuccessListener {
                Timber.tag("LoginFragment").d("User data created in Firestore")
            }
            .addOnFailureListener { e ->
                Timber.tag("LoginFragment").e(e, "Error creating user data")
            }
    }
}
