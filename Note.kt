/**
 MAD204 – Lab 4
 File: Note.kt
 Name: Jennyfer Parmar
 Student ID: A002021240
 Date: 07 December 2025

 Description:
 This file defines the Note data model used in the application.
 Each Note represents a single record stored in the Room database.
 The class uses Room annotations to define the entity and primary key.
*/

package com.example.lab4_java
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String
)