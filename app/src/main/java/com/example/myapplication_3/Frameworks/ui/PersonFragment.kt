package com.example.myapplication_3.frameworks.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication_3.controllers.PersonViewModel
import com.example.myapplication_3.databinding.FragmentPersonBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.concurrent.Executors

@AndroidEntryPoint
class PersonFragment : Fragment() {
    private var _binding: FragmentPersonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonViewModel by viewModels()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val executor = Executors.newFixedThreadPool(4)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Наблюдатели LiveData
        viewModel.personsLiveData.observe(viewLifecycleOwner) { persons ->
            binding.recyclerView.adapter = PersonAdapter(persons)
        }

        viewModel.personsLiveDataApi.observe(viewLifecycleOwner) { persons ->
            binding.recyclerView.adapter = PersonAdapter(persons)
        }

        viewModel.specialtiesLiveDataApi.observe(viewLifecycleOwner) { specialties ->
            specialties?.takeIf { it.isNotEmpty() }?.let {
                val randomId = (0 until it.size).random()
                val selectedSpecialtyId = it[randomId].id
                viewModel.loadPersonsFromNetwork(selectedSpecialtyId)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        executor.shutdown()
        _binding = null
    }
}