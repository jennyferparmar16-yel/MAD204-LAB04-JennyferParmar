/**
 MAD204 – Lab 4
 File: NoteDao.kt
 Name: Jennyfer Parmar
 Student ID: A002021240
 Date: 07 December 2025

 Description:
 This file defines the Data Access Object (DAO) for the Note entity.
 It declares methods for inserting, updating, deleting, and retrieving
 notes from the Room database. These methods are used by the application
 to perform database operations.
*/
package com.example.lab4_java

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
