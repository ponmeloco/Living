package de.living.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.living.R
import java.text.SimpleDateFormat


class AdapterRecyclerViewTasks(private val userTasksList: ArrayList<HashMap<String, String>>) :
    RecyclerView.Adapter<AdapterRecyclerViewTasks.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design_tasks, parent, false)
        return ViewHolder(view, mListener)
    }

    // binds the list items to a view
    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tasks = userTasksList[position]
        // sets the text to the textview from our itemHolder class
        holder.textViewTask.text = tasks["name"]
        holder.textViewRotation.text = tasks["memberToDo"]
        val timestamp = tasks["timeCreated"] as com.google.firebase.Timestamp
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = java.util.Date(milliseconds)
        val date = sdf.format(netDate).toString()
        Log.d("TAG170", date)
        holder.textViewTimeRemaining.text = date

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return userTasksList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(itemView: View, private var mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textViewTask: TextView = itemView.findViewById(R.id.textViewTask)
        val textViewRotation: TextView = itemView.findViewById(R.id.textViewRotation)
        val textViewTimeRemaining: TextView = itemView.findViewById(R.id.textViewTimeRemaining)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            mListener.setOnClickListener(adapterPosition)
        }
    }

    interface OnItemClickListener {
        fun setOnClickListener(pos: Int)
    }

    fun setOnItemClickListener(mListener: OnItemClickListener) {
        this.mListener = mListener
    }
}