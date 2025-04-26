package com.example.myapplication_3.frameworks.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication_3.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication_3.entities.ExpenseItem
import com.example.myapplication_3.frameworks.files.PDFGeneratorExpense
import com.example.myapplication_3.frameworks.files.XLSFileHandler
import com.example.myapplication_3.controllers.SharedFinanceViewModel
import com.example.myapplication_3.databinding.FragmentExpenseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseFragment : Fragment() {
    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseAdapter: ExpenseAdapter
    private val sharedFinanceViewModel: SharedFinanceViewModel by viewModels()
    private var selectedMenuItemPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = sharedFinanceViewModel
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        XLSFileHandler.initialize(requireContext(), "expenses.xls")
        expenseAdapter = ExpenseAdapter(mutableListOf(), sharedFinanceViewModel, this)

        binding.recyclerViewExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = expenseAdapter
        }

        PagerSnapHelper().attachToRecyclerView(binding.recyclerViewExpenses)
        ItemTouchHelper(createItemTouchHelperCallback()).attachToRecyclerView(binding.recyclerViewExpenses)
        registerForContextMenu(binding.recyclerViewExpenses)

        loadExpenses()
    }

    fun generatePdfReport() {
        PDFGeneratorExpense.generateAndShowPdf(requireContext(), expenseAdapter.expenseItems)
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                when (direction) {
                    ItemTouchHelper.DOWN -> expenseAdapter.deleteExpense(position)
                    ItemTouchHelper.UP -> expenseAdapter.showEditExpenseDialog(position)
                }
            }
        }
    }

    private fun loadExpenses() {
        expenseAdapter.expenseItems.clear()
        expenseAdapter.expenseItems.addAll(XLSFileHandler.loadDataFromXLS())
        expenseAdapter.notifyDataSetChanged()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun showContextMenuForItem(position: Int, view: View) {
        selectedMenuItemPosition = position
        view.showContextMenu()
        unregisterForContextMenu(view)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.context_menu_main, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_expense -> {
                showAddExpenseDialog()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("Добавить расход")
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
            setView(view)

            val inputExpense = view.findViewById<EditText>(R.id.input_expense)
            val inputDate = view.findViewById<EditText>(R.id.input_date_expense)
            val inputType = view.findViewById<EditText>(R.id.input_type_expense)

            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                inputDate.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time))
            }

            inputDate.setOnClickListener { DatePickerDialog(requireContext(), datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }

            setPositiveButton("OK") { _, _ ->
                val expense = inputExpense.text.toString().toDoubleOrNull()
                val date = inputDate.text.toString()
                val type = inputType.text.toString()

                if (expense != null && date.isNotEmpty() && type.isNotEmpty()) {
                    ExpenseItem(expense, date, type).let { item ->
                        expenseAdapter.addExpense(item)
                        XLSFileHandler.addLineToXLS(item)
                        sharedFinanceViewModel.addExpense(expense, date, type)
                        showToast("Ваш расход ${sharedFinanceViewModel.getTotalExpense()} руб")
                    }
                } else {
                    showToast("Заполните все поля корректно")
                }
            }
            setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    override fun onPause() {
        super.onPause()
        XLSFileHandler.saveDataToXLS(expenseAdapter.expenseItems.map {
            "${it.expense},${it.date},${it.type}"
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}