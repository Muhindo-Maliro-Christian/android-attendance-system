package com.example.attendancesystem.models.employee

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getEmployees(): Flow<List<Employee>>

    @Insert(onConflict  = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg blog : Employee)

}