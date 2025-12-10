/**
 MAD204 – Lab 4
 File: NotesDatabase.kt
 Name: Jennyfer Parmar
 Student ID: A002021240
 Date: 07 December 2025

 Description:
 This file defines the Room database configuration for the application.
 It provides a singleton instance of the database and access to NoteDao.
 The database is responsible for managing persistent storage of notes.
*/

package com.example.lab4_java

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {

    // THIS MUST BE ABSTRACT
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}