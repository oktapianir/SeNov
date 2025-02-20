package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.okta.senov.R
import com.okta.senov.databinding.FragmentAdminBinding
import timber.log.Timber

class AdminFragment : Fragment(R.layout.fragment_admin) {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.tag("AdminFragment").d("onCreateView called")
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        Timber.tag("AdminFragment").d("onDestroyView called")
        super.onDestroyView()
        _binding = null
    }
}
