//package com.example.myapplication_3.person
//import PersonAdapter
//import PersonViewModel
//import PersonViewModelFactory
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.ContentValues
//import android.content.Context
//import android.database.Cursor
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.view.ContextMenu
//import android.view.LayoutInflater
//import android.view.MenuInflater
//import android.view.MenuItem
//import android.view.View
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ProgressBar
//import android.widget.Toast
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.myapplication_3.BaseMenu
//import com.example.myapplication_3.BD.PersonDatabaseHelper
//import com.example.myapplication_3.R
//import kotlin.random.Random
//import kotlinx.coroutines.*
//import java.util.UUID
//import java.util.concurrent.Executors
//
//class PersonActivity : AppCompatActivity() {
//    private lateinit var recyclerView: RecyclerView
//    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private val viewModel: PersonViewModel by viewModels {
//        PersonViewModelFactory(PersonDatabaseHelper(this), this)
//    }
//    private val handler = Handler(Looper.getMainLooper())
//    private val executor = Executors.newFixedThreadPool(4)
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_for_sql)
//        recyclerView = findViewById(R.id.recycler_view)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//
//
//
//
//        viewModel.personsLiveDataApi.observe(this) { persons ->
//            val adapter = PersonAdapter(persons)
//            recyclerView.adapter = adapter
//        }
//
//
//        viewModel.specialtiesLiveDataApi.observe(this) { specialties ->
//            val randomId = Random.nextInt(specialties.size)
//            val selectedSpecialtyId = specialties[randomId].id
//            viewModel.loadPersonsFromNetwork(selectedSpecialtyId)
//        }
//
//
//        viewModel.isLoading.observe(this) { isLoading ->
//            val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
//            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
//
//        viewModel.errorMessage.observe(this) { errorMessage ->
//            if (errorMessage != null) {
//                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
//                viewModel.clearErrorMessage()
//            }
//        }
//
//
//        val loadPersonsButton = findViewById<Button>(R.id.load_persons_button) // Предполагается, что у вас есть кнопка с этим ID
//        loadPersonsButton.setOnClickListener {
//            viewModel.cancelRequests()
//            viewModel.loadSpecialties()
//        }
//
//        val buttonStopRetrofit = findViewById<Button>(R.id.stop_load_button)
//        buttonStopRetrofit.setOnClickListener {
//            viewModel.cancelRequests()
//            Toast.makeText(this@PersonActivity, "Загрузка остановлена", Toast.LENGTH_SHORT).show()
//        }
//
//
//
//
//
//
//        val cancelButton = findViewById<Button>(R.id.cancel_button)
//        cancelButton.setOnClickListener {
//            viewModel.clearData()
//        }
//
//
//        val buttonGenerateCoroutines = findViewById<Button>(R.id.button_generate_data)
//        buttonGenerateCoroutines.setOnClickListener {
//            generateDataWithCoroutines()
//        }
//
//        val buttonGenerateTreads = findViewById<Button>(R.id.button_generate_data_with_tread)
//        buttonGenerateTreads.setOnClickListener {
//            generateDataWithThreads()
//        }
//
//        val buttonStop = findViewById<Button>(R.id.stop_button)
//        buttonStop.setOnClickListener {
//            viewModel.stopDataGeneration()
//            Toast.makeText(this@PersonActivity, "Генерация приостановлена", Toast.LENGTH_SHORT).show()
//        }
//
//        viewModel.personsLiveData.observe(this) { persons ->
//            val adapter = PersonAdapter(persons)
//            recyclerView.adapter = adapter
//        }
//
//        if (viewModel.personsLiveData.value == null) {
//            viewModel.loadPersons()
//        }
//
//
//    }
//
////    override fun getLayoutResId(): Int {
////        return R.layout.activity_for_sql
////    }
//
//    private fun generateDataWithCoroutines() {
//        val button = findViewById<Button>(R.id.button_generate_data)
//
//        button.isEnabled = false
//        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.VISIBLE
//
//        coroutineScope.launch {
//            withContext(Dispatchers.IO) {
//                viewModel.generateAndInsertData(10000)
//            }
//
//
//            withContext(Dispatchers.Main) {
//                viewModel.loadPersons()
//                button.isEnabled = true
//                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
//                Toast.makeText(this@PersonActivity, "Данные сгенерированы и добавлены", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun generateDataWithThreads() {
//        val button = findViewById<Button>(R.id.button_generate_data_with_tread)
//
//        button.isEnabled = false
//        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.VISIBLE
//
//        executor.execute {
//            viewModel.generateAndInsertData(10000)
//
//
//            handler.post {
//                button.isEnabled = true
//                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
//                Toast.makeText(this@PersonActivity, "Данные сгенерированы и добавлены", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        coroutineScope.cancel()
//        executor.shutdown()
//    }
//}