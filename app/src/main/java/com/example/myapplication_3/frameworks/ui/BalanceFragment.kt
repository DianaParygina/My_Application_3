package com.example.myapplication_3.frameworks.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication_3.controllers.SharedFinanceViewModel
import com.example.myapplication_3.databinding.FragmentBalanceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BalanceFragment : Fragment() {
    private var _binding: FragmentBalanceBinding? = null
    private val binding get() = _binding!!
    private val sharedFinanceViewModel: SharedFinanceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBalanceBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = sharedFinanceViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}