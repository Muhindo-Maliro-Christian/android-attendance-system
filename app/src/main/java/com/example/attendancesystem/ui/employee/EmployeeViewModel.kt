package com.example.attendancesystem.ui.employee

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.attendancesystem.models.employee.Employee
import com.example.attendancesystem.repositories.EmployeeRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect


class EmployeeViewModel(private val repository: EmployeeRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    var employees : MutableLiveData<List<Employee>> = MutableLiveData<List<Employee>>()


    init{
        getAllEmployees()
    }

    private fun getAllEmployees() {
        viewModelScope.launch {
            repository.employees.collect { it ->
                employees.value = it
            }
        }
    }

    fun insert(employee: Employee) = viewModelScope.launch {
        repository.insert(employee)
    }

}

class EmployeeViewModelFactory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


