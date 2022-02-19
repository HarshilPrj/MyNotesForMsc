package com.example.mynotesformsc.Adapter

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesformsc.Model.Notes
import com.example.mynotesformsc.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot

class NoteRecyclerAdapter(options: FirebaseRecyclerOptions<Notes>,val noteListner:NoteListner) :
    FirebaseRecyclerAdapter<Notes, NoteRecyclerAdapter.NoteViewHolder>(options) {

    class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val txtNote:TextView = itemView.findViewById(R.id.txtNote)
        val txtDate:TextView = itemView.findViewById(R.id.txtDate)
        val cbIsCompleted:CheckBox = itemView.findViewById(R.id.cbIsCompleted)
        val itemLayout:LinearLayout = itemView.findViewById(R.id.itemLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, notes: Notes) {
        holder.txtNote.setText(notes.text)

        val date:CharSequence = android.text.format.DateFormat.format("EEEE, MMM d,yyyy h:mm:ss",notes.currentTime!!)
        holder.txtDate.setText(date)
        holder.cbIsCompleted.isChecked = notes.isCompleted!!

        holder.cbIsCompleted.setOnCheckedChangeListener { compoundButton, b ->
            val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            noteListner.handleCheckedchange(b,dataSnapshot)
        }
        holder.itemLayout.setOnClickListener {
            val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            noteListner.handleEditClickListener(dataSnapshot)
        }
    }

    public fun deleteItem(position: Int){
        Log.d("DeleteItem",position.toString())
        noteListner.handleDeleteListener(snapshots.getSnapshot(position))
    }

    public fun editItem(position: Int){
        noteListner.handleDeleteListener(snapshots.getSnapshot(position))
    }

    interface NoteListner{
        public fun handleCheckedchange(isCheck:Boolean,dataSnapshot: DataSnapshot)
        public fun handleEditClickListener(dataSnapshot: DataSnapshot)
        public fun handleDeleteListener(dataSnapshot: DataSnapshot)
    }
}


