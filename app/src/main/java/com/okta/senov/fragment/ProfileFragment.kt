package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.okta.senov.R
import com.okta.senov.databinding.FragmentProfileBinding
import com.okta.senov.extensions.findNavController

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var auth: FirebaseAuth? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
        }

        auth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUsername.text = getString(R.string.oktapiani_r)
        binding.tvEmail.text = getString(R.string.ramdhanioktapiani_gmail_com)

        binding.icBack.setOnClickListener {
            binding.icBack.findNavController().navigateUp()
        }

        binding.icLogout.setOnClickListener {
            val currentAuth = auth
            if (currentAuth != null) {
                currentAuth.signOut()

                Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Error: FirebaseAuth not initialized", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
