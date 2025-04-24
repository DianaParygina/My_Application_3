package com.example.myapplication_3.Frameworks.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.R
import com.example.myapplication_3.Controllers.PersonViewModel
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
    private lateinit var recyclerView: RecyclerView
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val viewModel: PersonViewModel by viewModels()
    private val executor = Executors.newFixedThreadPool(4)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Наблюдатели LiveData
        viewModel.personsLiveData.observe(viewLifecycleOwner) { persons ->
            recyclerView.adapter = PersonAdapter(persons)
        }

        viewModel.personsLiveDataApi.observe(viewLifecycleOwner) { persons ->
            recyclerView.adapter = PersonAdapter(persons)
        }

        viewModel.specialtiesLiveDataApi.observe(viewLifecycleOwner) { specialties ->
            specialties?.takeIf { it.isNotEmpty() }?.let {
                val randomId = (0 until it.size).random()
                val selectedSpecialtyId = it[randomId].id
                viewModel.loadPersonsFromNetwork(selectedSpecialtyId)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            view.findViewById<ProgressBar>(R.id.progress_bar).visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }

        // Обработчики кнопок
        view.findViewById<Button>(R.id.load_persons_button).setOnClickListener {
            viewModel.cancelRequests()
            viewModel.loadSpecialties()
        }

        view.findViewById<Button>(R.id.stop_load_button).setOnClickListener {
            viewModel.cancelRequests()
            Toast.makeText(requireContext(), "Загрузка остановлена", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            viewModel.clearData()
        }

        view.findViewById<Button>(R.id.button_generate_data).setOnClickListener {
            generateDataWithCoroutines()
        }

        view.findViewById<Button>(R.id.button_generate_data_with_tread).setOnClickListener {
            generateDataWithThreads()
        }

        view.findViewById<Button>(R.id.stop_button).setOnClickListener {
            viewModel.stopDataGeneration()
            Toast.makeText(requireContext(), "Генерация приостановлена", Toast.LENGTH_SHORT).show()
        }

        // Первоначальная загрузка данных
        if (viewModel.personsLiveData.value == null) {
            viewModel.loadPersons()
        }
    }

    private fun generateDataWithCoroutines() {
        val button = requireView().findViewById<Button>(R.id.button_generate_data)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.progress_bar)

        button.isEnabled = false
        progressBar.visibility = View.VISIBLE

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.generateAndInsertData(10000)
            }
            withContext(Dispatchers.Main) {
                viewModel.loadPersons()
                button.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Данные сгенерированы (Coroutines)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateDataWithThreads() {
        val button = requireView().findViewById<Button>(R.id.button_generate_data_with_tread)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.progress_bar)

        button.isEnabled = false
        progressBar.visibility = View.VISIBLE

        executor.execute {
            viewModel.generateAndInsertData(10000)
            requireActivity().runOnUiThread {
                viewModel.loadPersons()
                button.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Данные сгенерированы (Threads)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        executor.shutdown()
    }
}