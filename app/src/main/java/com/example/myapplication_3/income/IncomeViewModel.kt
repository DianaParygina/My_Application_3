//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import com.example.myapplication_3.income.Income
//import com.example.myapplication_3.income.IncomeType
//import kotlinx.coroutines.launch
//
//class IncomeViewModel(application: Application, private val incomeRepository: IncomeRepository) : AndroidViewModel(application) {
//
//    // LiveData для отображения доходов
//    private val _incomes = MutableLiveData<List<Income>>()
//    val incomes: LiveData<List<Income>> = _incomes
//
//    // LiveData для отображения типов доходов
//    private val _incomeTypes = MutableLiveData<List<IncomeType>>()
//    val incomeTypes: LiveData<List<IncomeType>> = _incomeTypes
//
//    // LiveData для отображения состояния загрузки/ошибок
//    private val _dataLoading = MutableLiveData<Boolean>(false)
//    val dataLoading: LiveData<Boolean> = _dataLoading
//
//    private val _error = MutableLiveData<String?>(null)
//    val error: LiveData<String?> = _error
//
//
//    init {
//        loadData()
//    }
//
//    fun loadData() {
//        viewModelScope.launch {
//            _dataLoading.value = true
//            try {
//                _incomes.value = incomeRepository.getIncomes()
//                _incomeTypes.value = incomeRepository.getIncomeTypes()
//            } catch (e: Exception) {
//                _error.value = e.message //  Обработка ошибок
//            } finally {
//                _dataLoading.value = false
//            }
//        }
//    }
//
//    fun addIncome(income: Income) {
//        viewModelScope.launch {
//            try {
//                incomeRepository.addIncome(income)
//                //  Рекомендуется обновить данные после добавления, но можно использовать и postValue для лучшей производительности
//                loadData()
//            } catch (e: Exception) {
//                _error.value = e.message
//            }
//        }
//    }
//
//    fun deleteIncome(income: Income) {
//        viewModelScope.launch {
//            try {
//                incomeRepository.deleteIncome(income)
//                loadData()
//            } catch (e: Exception) {
//                _error.value = e.message
//            }
//        }
//    }
//
//    fun updateIncome(income: Income) {
//        viewModelScope.launch {
//            try {
//                incomeRepository.updateIncome(income)
//                loadData()
//            } catch (e: Exception) {
//                _error.value = e.message
//            }
//        }
//    }
//
//    fun addIncomeType(incomeType: IncomeType) {
//        viewModelScope.launch {
//            try {
//                incomeRepository.insertIncomeType(incomeType)
//                loadData() //  Обновляем список типов доходов
//            } catch (e: Exception) {
//                _error.value = e.message
//            }
//        }
//    }
//}