package de.living.mainApp

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.living.adapter.AdapterRecyclerViewTasks
import de.living.databinding.FragmentCleaningBinding
import de.living.model.GroupsList
import de.living.viewmodel.UserDataViewModel


class CleaningFragment : Fragment() {

    private var _binding: FragmentCleaningBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val _userDataViewModel: UserDataViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleaningBinding.inflate(inflater, container, false)
        val root: View = binding.root
        inflateRecyclerview()
        inflateGroupSpinner()
        buttonEffect(binding.markAsFinishedButton)


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun inflateRecyclerview(){
        // getting the recyclerview by its id
        val recyclerview = binding.recyclerViewTasks

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(binding.recyclerViewTasks.context)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<GroupsList>()

        for (i in 1..10) {
            _userDataViewModel.getGroups().value?.let { GroupsList(it.Gruppe1) }
                ?.let { data.add(it) }
        }
        // This will pass the ArrayList to our Adapter
        val adapter = AdapterRecyclerViewTasks(data)

        adapter.setOnItemClickListener(object : AdapterRecyclerViewTasks.OnItemClickListener{
            override fun setOnClickListener(pos: Int) {

            }
        })

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
    }


    private fun inflateGroupSpinner(){
        val spinner = binding.groupSpinner
        val groupNames = ArrayList<String?>()

        for (i in 1..5) {
            groupNames.add(_userDataViewModel.getGroups().value?.Gruppe1)
            val adapter = this@CleaningFragment.context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_item, groupNames
                )
            }
            spinner.adapter = adapter

        }
    }

    @Suppress("DEPRECATION","ClickableViewAccessibility")
    fun buttonEffect(button: View) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.background.setColorFilter(Color.parseColor("#FFCD58"), PorterDuff.Mode.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            false
        }
    }

}