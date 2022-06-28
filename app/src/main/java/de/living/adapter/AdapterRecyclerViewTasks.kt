package de.living.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.living.R
import de.living.model.GroupsList


class AdapterRecyclerViewTasks(private val userTasksList: ArrayList<GroupsList>) : RecyclerView.Adapter<AdapterRecyclerViewTasks.ViewHolder>() {

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
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tasks = userTasksList[position]
        // sets the text to the textview from our itemHolder class
        holder.textViewTask.text = tasks.Gruppe1
        holder.textViewRotation.text = tasks.Gruppe1
        holder.textViewTimeRemaining.text = tasks.Gruppe1

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return userTasksList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(itemView: View, var mListener:OnItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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

    interface OnItemClickListener{
        fun setOnClickListener(pos : Int)
    }

    fun setOnItemClickListener(mListener: OnItemClickListener){
        this.mListener = mListener
    }
}