package com.example.myapplication_3.Frameworks.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication_3.R
import com.example.myapplication_3.Controllers.PersonViewModel
import com.example.myapplication_3.databinding.FragmentPersonBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@AndroidEntryPoint
class PersonFragment : Fragment() {
    private var _binding: FragmentPersonBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val viewModel: PersonViewModel by viewModels()
    private val executor = Executors.newFixedThreadPool(4)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonBinding.inflate(inflater, container, false)
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

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }

        // Обработчики кнопок
        binding.loadPersonsButton.setOnClickListener {
            viewModel.cancelRequests()
            viewModel.loadSpecialties()
        }

        binding.stopLoadButton.setOnClickListener {
            viewModel.cancelRequests()
            Toast.makeText(requireContext(), "Загрузка остановлена", Toast.LENGTH_SHORT).show()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.clearData()
        }

        binding.buttonGenerateData.setOnClickListener {
            generateDataWithCoroutines()
        }

        binding.buttonGenerateDataWithTread.setOnClickListener {
            generateDataWithThreads()
        }

        binding.stopButton.setOnClickListener {
            viewModel.stopDataGeneration()
            Toast.makeText(requireContext(), "Генерация приостановлена", Toast.LENGTH_SHORT).show()
        }

        // Первоначальная загрузка данных
        if (viewModel.personsLiveData.value == null) {
            viewModel.loadPersons()
        }
    }

    private fun generateDataWithCoroutines() {
        binding.buttonGenerateData.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.generateAndInsertData(10000)
            }
            withContext(Dispatchers.Main) {
                viewModel.loadPersons()
                binding.buttonGenerateData.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Данные сгенерированы (Coroutines)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateDataWithThreads() {
        binding.buttonGenerateDataWithTread.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        executor.execute {
            viewModel.generateAndInsertData(10000)
            requireActivity().runOnUiThread {
                viewModel.loadPersons()
                binding.buttonGenerateDataWithTread.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Данные сгенерированы (Threads)",
                    Toast.LENGTH_SHORT
                ).show()
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