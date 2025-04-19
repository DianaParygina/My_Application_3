package com.example.myapplication_3.Frameworks.ui

import com.example.myapplication_3.External.SharedFinanceViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication_3.R
import com.example.myapplication_3.Controllers.SharedFinanceViewModel
import com.example.myapplication_3.repository.ExpenseRepositoryImpl
import com.example.myapplication_3.repository.IncomeRepositoryImpl

class BalanceFragment : Fragment() {

    private lateinit var tvBalance: TextView
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_balance, container, false)

        tvBalance = view.findViewById(R.id.tvBalance)
        val incomeRepository = IncomeRepositoryImpl(requireContext())
        val expenseRepository = ExpenseRepositoryImpl(requireContext())
        sharedFinanceViewModel = ViewModelProvider(this,
            SharedFinanceViewModelFactory(
                requireActivity().application,
                incomeRepository,
                expenseRepository
            )
        ).get(SharedFinanceViewModel::class.java)

        sharedFinanceViewModel.totalBalance.observe(viewLifecycleOwner, Observer { newBalance ->
            tvBalance.text = "Ваш баланс: ${newBalance} руб"
        })

        return view
    }
}