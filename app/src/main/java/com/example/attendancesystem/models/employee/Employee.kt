package com.example.attendancesystem.models.employee

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "employees")
class Employee (
    @PrimaryKey val id : Int,
    @ColumnInfo val name : String,
    @ColumnInfo val sexe : String,
    @ColumnInfo val adresse : String,
    @ColumnInfo val promotion : String,
    @ColumnInfo val annee_academique : String,
)