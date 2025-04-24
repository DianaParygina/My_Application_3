package com.example.myapplication_3.Frameworks.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.myapplication_3.R
import com.example.myapplication_3.Controllers.SharedFinanceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BalanceFragment : Fragment() {

    private lateinit var tvBalance: TextView
    private val sharedFinanceViewModel: SharedFinanceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_balance, container, false)

        tvBalance = view.findViewById(R.id.tvBalance)

        sharedFinanceViewModel.totalBalance.observe(viewLifecycleOwner, Observer { newBalance ->
            tvBalance.text = "Ваш баланс: ${newBalance} руб"
        })

        return view
    }
}