package com.example.attendancesystem.repositories

import androidx.annotation.WorkerThread
import com.example.attendancesystem.models.employee.Employee
import com.example.attendancesystem.models.employee.EmployeeDao
import kotlinx.coroutines.flow.Flow

class EmployeeRepository (private val employeeDao: EmployeeDao){
    val employees: Flow<List<Employee>> = employeeDao.getEmployees()
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(employee : Employee) {
        employeeDao.insert(employee)
    }

}