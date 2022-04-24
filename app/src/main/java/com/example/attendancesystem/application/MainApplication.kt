package com.example.attendancesystem.application

import android.app.Application
import com.example.attendancesystem.models.employee.EmployeeDatabase
import com.example.attendancesystem.repositories.EmployeeRepository

class MainApplication : Application(){
    val database by lazy { EmployeeDatabase.getDatabase(this) }
    val repository by lazy { EmployeeRepository(database.employeeDao()) }
}