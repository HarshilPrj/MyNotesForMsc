package com.example.mynotesformsc.Activity

import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Adapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesformsc.Adapter.NoteRecyclerAdapter
import com.example.mynotesformsc.Model.LoginActivity
import com.example.mynotesformsc.Model.Notes
import com.example.mynotesformsc.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.collections.HashMap
import android.graphics.Canvas

import androidx.core.content.ContextCompat
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class MainActivity : AppCompatActivity(),NoteRecyclerAdapter.NoteListner {

    val TAG = "MainActivity"
    lateinit var fab:FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var notesAdapter:NoteRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser == null){
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        initRecyclerAdapter()
    }

    private fun initRecyclerAdapter() {
        val query:Query = FirebaseDatabase.getInstance().reference
            .child("Notes")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        val options:FirebaseRecyclerOptions<Notes> = FirebaseRecyclerOptions.Builder<Notes>()
            .setQuery(query,Notes::class.java)
            .build()

        notesAdapter = NoteRecyclerAdapter(options, this)

        recyclerView.adapter = notesAdapter

        var itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    var simpleCallback:ItemTouchHelper.SimpleCallback =
        object:ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                TODO("not")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.RIGHT){
                    Toast.makeText(this@MainActivity,"Swipe To Right",Toast.LENGTH_LONG).show()
                    notesAdapter.deleteItem(viewHolder.adapterPosition)
                }else{
                    Toast.makeText(this@MainActivity,"Swipe To Left",Toast.LENGTH_LONG).show()
                    notesAdapter.editItem(viewHolder.adapterPosition)
                }
            }

            override fun onChildDraw(c: Canvas,recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder,dX:Float,dY:Float,actionState:Int,isCurrentlyActive:Boolean){
               RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.Red))
                    .addActionIcon(R.drawable.ic_baseline_delete)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

    override fun onStart() {
        super.onStart()

        fab.setOnClickListener {
            createAlertDialog()
        }

        notesAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notesAdapter.startListening()
    }

    private fun createAlertDialog() {

        var editText:EditText = EditText(this)


        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(editText)
            .setPositiveButton("Add",object : DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    //when user click on add button
                    addNotestoFirebasedatabase(editText.text.toString())
                }
            })
            .setNegativeButton("CANCEL",null)
            .create()
            .show()
    }

    private fun addNotestoFirebasedatabase(text:String) {
        val ref = FirebaseDatabase.getInstance().reference

        val notes= Notes(text,
            false,
            System.currentTimeMillis())

        ref.child("Notes")
            .child(FirebaseAuth.getInstance().uid.toString())
            .child(UUID.randomUUID().toString())
            .setValue(notes)
            .addOnSuccessListener {
                Log.d(TAG,"addOnSuccessListener : Notes Added Successfully.")
                Toast.makeText(this,"Notes Add Successfully",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Log.d(TAG,"addOnSuccessListener : ${it.message}")
                Toast.makeText(this,"Error : ${it.message}",Toast.LENGTH_LONG).show()
            }
    }

    fun startLoginActivity() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_logout -> {
                //logout from Activity
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            startLoginActivity()
                        }else{
                            Log.d(TAG,"addOnCompleteListener : ${it.exception}")
                        }
                    }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleCheckedchange(isCheck: Boolean, dataSnapshot: DataSnapshot) {
        Log.d("MainActivity","Checked Change")

        val mapOf = HashMap<String,Any>()
        mapOf.put("completed",isCheck)

        dataSnapshot.ref.updateChildren(mapOf)
            .addOnSuccessListener {
                Log.d("MainActivity","onSuccess: Checkbox updated")
            }
            .addOnSuccessListener {
                Log.d("MainActivity","onFailure: Checkbox is not Updated")
            }
    }

    override fun handleEditClickListener(dataSnapshot: DataSnapshot) {
        Log.d("MainActivity","Edit Item")

        val note = dataSnapshot.getValue<Notes>()
        val editText:EditText = EditText(this)
        editText.setText(note!!.text)
        editText.setSelection(note.text!!.length)

        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(editText)
            .setPositiveButton("Done"){dialogInterface,i ->
                //perform the action
                val newNoteText = editText.text.toString()
                note.text = newNoteText

                dataSnapshot.ref.setValue(note)
                    .addOnSuccessListener {
                        Log.d("MainActivity","onSuccess: Note Updated")
                    }
                    .addOnFailureListener {
                        Log.d("MainActivity","onFailure: Note is not Updated")
                    }
            }
            .setNegativeButton("Cancel",null)
            .show()
    }

    override fun handleDeleteListener(dataSnapshot: DataSnapshot) {
        dataSnapshot.ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this,"Notes Deleted Successfully",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Notes Not Deleted",Toast.LENGTH_LONG).show()
            }
    }
}