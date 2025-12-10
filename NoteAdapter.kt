/**
 MAD204 – Lab 4
 File: NoteAdapter.kt
 Name: Jennyfer Parmar
 Student ID: A002021240
 Date: 07 December 2025

 Description:
 This file defines a RecyclerView adapter used to display notes.
 It converts Note objects into list items shown on the screen.
 The adapter handles click events for editing notes and long-click
 events for deleting notes.
*/
package com.example.lab4_java

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/*
 MAD204 – Lab 4
 File: NoteAdapter.kt
 Name: Jennyfer Parmar
 Student ID: A002021240
 Date: 07 December 2025

 Description:
 This file defines a RecyclerView adapter for displaying notes.
 It uses item_note.xml to show each note's title and content.
 It handles click events for editing and long-click events for deleting notes.
*/

class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // Holds references to views in item_note.xml
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // Inflate item_note.xml for each row
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        // Click to edit
        holder.itemView.setOnClickListener { onItemClick(note) }

        // Long click to delete
        holder.itemView.setOnLongClickListener {
            onItemLongClick(note)
            true
        }
    }

    override fun getItemCount(): Int = notes.size

    // Replace current list with a new one
    fun setData(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}
