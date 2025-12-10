/**
  MAD204 Java Development – Lab 4
  File: MainActivity.kt
  Author: Jennyfer Parmar
  Date: 07/12/2025


  MainActivity:
   - Adds new notes to the Room database
   - Displays notes in a RecyclerView using NoteAdapter
   - Edits notes in a custom AlertDialog
   - Deletes notes with an Undo action using Snackbar
   - Starts ReminderService when the user presses the reminder button
 */

package com.example.lab4_java

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// Room Entity: Note


/**
 * Note entity stored in the Room database.
 *
 * Each note has:
 *  - id      : primary key, auto-generated
 *  - title   : short text for the note title
 *  - content : longer text for the note body
 *
 * Room will create a table called "notes_table" with these columns.
 */
@Entity(tableName = "notes_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String
)


// Room DAO: NoteDao


/**
 * DAO (Data Access Object) for Note.
 *
 * Provides all CRUD operations used in the app:
 *  - getAllNotes(): read all notes ordered by id (newest first)
 *  - insert(): insert or replace a note
 *  - update(): update an existing note
 *  - delete(): delete a note
 *
 * All functions are suspend so they can be called from coroutines.
 */
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes_table ORDER BY id DESC")
    suspend fun getAllNotes(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}


// Room Database: NotesDatabase


/**
 * Room database that holds the notes_table.
 *
 * - Registers the Note entity
 * - Exposes NoteDao via abstract fun noteDao()
 * - Uses a companion object to implement a singleton database instance.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


// RecyclerView Adapter: NoteAdapter


/**
 * RecyclerView adapter for displaying notes.
 *
 * Uses the built-in layout android.R.layout.simple_list_item_2 which has:
 *  - text1 (used for title)
 *  - text2 (used for content)
 *
 * Two click callbacks:
 *  - onItemClick(note)     : for editing a note
 *  - onItemLongClick(note) : for deleting a note
 */
class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(android.R.id.text1)
        val contentTextView: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        holder.itemView.setOnClickListener { onItemClick(note) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(note)
            true
        }
    }

    override fun getItemCount(): Int = notes.size

    /**
     * Replaces the adapter data with a new list of notes.
     */
    fun setData(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}


// Service: ReminderService


/**
 * Simple Service stub used to satisfy the lab requirement of starting a Service.
 *
 * Currently it does not perform any background work and only implements onBind().
 * It can be extended later to show timed reminders or notifications.
 */
class ReminderService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}


// Activity: MainActivity


/**
 * MainActivity
 *
 * Connects the UI layer with the Room database layer.
 * Handles:
 *  - adding new notes
 *  - loading notes into RecyclerView
 *  - editing notes in a dialog
 *  - deleting notes with Snackbar Undo
 *  - starting the ReminderService when requested.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var db: NotesDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var adapter: NoteAdapter

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var addNoteButton: Button
    private lateinit var reminderButton: Button
    private lateinit var notesRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Room database and DAO
        db = NotesDatabase.getInstance(this)
        noteDao = db.noteDao()

        // Bind UI views
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        addNoteButton = findViewById(R.id.addNoteButton)
        reminderButton = findViewById(R.id.reminderButton)
        notesRecyclerView = findViewById(R.id.notesRecyclerView)

        // Set up RecyclerView and adapter
        adapter = NoteAdapter(
            mutableListOf(),
            onItemClick = { note -> showEditDialog(note) },
            onItemLongClick = { note -> deleteNoteWithUndo(note) }
        )
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = adapter

        // Add note button
        addNoteButton.setOnClickListener { addNote() }

        // Start ReminderService button
        reminderButton.setOnClickListener {
            val serviceIntent = Intent(this, ReminderService::class.java)
            startService(serviceIntent)
        }

        // Load existing notes from database
        loadNotes()
    }


    //  Loads all notes from Room and updates the RecyclerView adapter.

    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = withContext(Dispatchers.IO) {
                noteDao.getAllNotes()
            }
            adapter.setData(notes)
        }
    }

    //Reads text from the input fields and inserts a new Note into the database.

    private fun addNote() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        if (title.isEmpty() || content.isEmpty()) return

        lifecycleScope.launch {
            val note = Note(title = title, content = content)
            withContext(Dispatchers.IO) {
                noteDao.insert(note)
            }
            titleEditText.text.clear()
            contentEditText.text.clear()
            loadNotes()
        }
    }

    // Shows an AlertDialog that lets the user edit the selected note.

    private fun showEditDialog(note: Note) {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_note, null)

        // Get references to EditTexts inside the dialog
        val dialogTitleEditText =
            dialogView.findViewById<EditText>(R.id.dialogTitleEditText)
        val dialogContentEditText =
            dialogView.findViewById<EditText>(R.id.dialogContentEditText)

        // Pre-fill fields with existing note data
        dialogTitleEditText.setText(note.title)
        dialogContentEditText.setText(note.content)

        // Build and show the AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedNote = note.copy(
                    title = dialogTitleEditText.text.toString(),
                    content = dialogContentEditText.text.toString()
                )
                // Update in database on background thread
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        noteDao.update(updatedNote)
                    }
                    loadNotes()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }




    /**
     * Deletes a note from the database and shows a Snackbar with an Undo option.
     */
    private fun deleteNoteWithUndo(note: Note) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                noteDao.delete(note)
            }
            loadNotes()

            Snackbar.make(notesRecyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            noteDao.insert(note)
                        }
                        loadNotes()
                    }
                }
                .show()
        }
    }
}
