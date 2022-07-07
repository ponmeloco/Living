package de.living.mainApp


import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import de.living.EditTextDialog
import de.living.adapter.AdapterRecyclerViewTasks
import de.living.databinding.FragmentCleaningBinding
import de.living.viewmodel.UserDataViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit


class CleaningFragment : Fragment() {
    private var _binding: FragmentCleaningBinding? = null
    private var selectedItem: Int = -1
    private val data = ArrayList<HashMap<String, String>>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: UserDataViewModel by activityViewModels()
    private lateinit var adapterRecyclerViewTasks: AdapterRecyclerViewTasks
    private var arrayAdapterSpinner: ArrayAdapter<String>? = null
    private lateinit var groupNames: ArrayList<String>


    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleaningBinding.inflate(inflater, container, false)
        val root: View = binding.root

        inflateGroupSpinner()

        // getting the recyclerview by its id
        val recyclerview = binding.recyclerViewTasks
        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(binding.recyclerViewTasks.context)
        // ArrayList of class ItemsViewModel
        if (!viewModel.bigUser.value?.tasksPerGroup.isNullOrEmpty()) {
            viewModel.bigUser.value?.email?.let {
                viewModel.getTasks(it)
                    ?.forEach { item ->
                        data.add(item)
                    }
            }
        }
        // This will pass the ArrayList to our Adapter
        adapterRecyclerViewTasks = AdapterRecyclerViewTasks(data)
        adapterRecyclerViewTasks.setOnItemClickListener(object : AdapterRecyclerViewTasks.OnItemClickListener {
            override fun setOnClickListener(pos: Int) {
                if (recyclerview.getChildAt(pos).isActivated) {
                    recyclerview.getChildAt(pos).isActivated = false
                    selectedItem = -1
                } else {
                    if(selectedItem != -1) {
                        recyclerview.getChildAt(selectedItem).isActivated = false
                    }
                    selectedItem = pos
                    recyclerview.getChildAt(selectedItem).isActivated = true
                }
            }
        })
        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapterRecyclerViewTasks

        binding.editTaskButton.setOnClickListener {
            buttonEffect(binding.markAsFinishedButton)
            if (selectedItem != -1) {
                val alertDialog = AlertDialog.Builder(activity)
                alertDialog.apply {
                    setMessage("Do you want to delete the task?")
                    setPositiveButton("Yes!") { _, _ ->
                        if (selectedItem != -1) {
                            recyclerview.getChildAt(selectedItem).isActivated = false
                            viewModel.deleteTask(
                                selectedItem,
                                binding.groupSpinner.selectedItem.toString()
                            )
                            binding.groupSpinner.setSelection(0)
                            data.clear()
                            viewModel.getTasks(binding.groupSpinner.selectedItem.toString())
                                ?.let { it1 -> data.addAll(it1) }
                            adapterRecyclerViewTasks.notifyDataSetChanged()
                            selectedItem = -1
                            Toast.makeText(activity, "Task deleted!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    setNegativeButton("No!") { _, _ ->
                    }
                }.create().show()
            }
        }

        binding.markAsFinishedButton.setOnClickListener {
            buttonEffect(binding.markAsFinishedButton)
            if (selectedItem != -1) {
                recyclerview.getChildAt(selectedItem).isActivated = false
                viewModel.markTaskAsFinished(
                    selectedItem,
                    binding.groupSpinner.selectedItem.toString()
                )
                binding.groupSpinner.setSelection(0)
                data.clear()
                viewModel.getTasks(binding.groupSpinner.selectedItem.toString())
                    ?.let { it1 -> data.addAll(it1) }
                adapterRecyclerViewTasks.notifyDataSetChanged()

                selectedItem = -1
                Toast.makeText(activity, "Next User is on the task!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "No group selected", Toast.LENGTH_SHORT).show()
            }

        }

        binding.addButton.setOnClickListener {
            val groupName = binding.groupSpinner.selectedItem as String
            val dialog =
                EditTextDialog.newInstance(text = "", hint = "Task name", isMultiline = false)
            dialog.onOk = {
                val taskName = dialog.editText.text.toString()
                viewModel.getUser().value?.let { user ->
                    viewModel.createTask(
                        taskName, user.email,
                        (groupName)
                    )
                }
                val memberOnTask = viewModel.getTasks(groupName)?.get(selectedItem)?.get("memberToDo")
                var indexOfNext =
                    viewModel.getGroupMemberNames(groupName)?.indexOf(memberOnTask)?.plus(1)
                if (indexOfNext != null) {
                    if(indexOfNext >= viewModel.getGroupMemberNames(groupName)?.size!!){
                        indexOfNext = 0
                    }
                }
                val nextName = indexOfNext?.let { viewModel.bigUser.value?.memberPerGroup?.get(groupName)?.get(it) } as String
                val seconds1: Long = Timestamp.now().seconds
                val addedSeconds1 = Instant.ofEpochSecond(seconds1).plus(7, ChronoUnit.DAYS).epochSecond
                val newTimeStamp1 = Timestamp(addedSeconds1, 0)
                val mapOfTask = hashMapOf(
                    "name" to taskName,
                    "memberToDo" to nextName,
                    "timeCreated" to Timestamp.now(),
                    "timeDeadline" to newTimeStamp1
                )
                data.add(mapOfTask as HashMap<String,String>)
                adapterRecyclerViewTasks.notifyDataSetChanged()
                binding.groupSpinner.setSelection(0)
            }
            dialog.show(parentFragmentManager, "editDescription")
        }

        binding.groupSpinner.setSelection(0)
        binding.groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                data.clear()
                viewModel.getTasks((binding.groupSpinner.getItemAtPosition(position).toString()))
                    ?.let { data.addAll(it) }
                binding.recyclerViewTasks.adapter?.notifyDataSetChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                binding.groupSpinner.setSelection(selectedItem)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun inflateGroupSpinner() {
        if (viewModel.bigUser.value?.groupNames != null) {
            val spinner = binding.groupSpinner
            groupNames = viewModel.bigUser.value?.groupNames as ArrayList<String>
            arrayAdapterSpinner = this@CleaningFragment.context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_dropdown_item, groupNames
                )
            }
            spinner.adapter = arrayAdapterSpinner
            binding.groupSpinner.setSelection(0)
            selectedItem = 0
        }


    }
}


@Suppress("DEPRECATION", "ClickableViewAccessibility")
fun buttonEffect(button: View) {
    button.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.background.setColorFilter(
                    Color.parseColor("#FFCD58"),
                    PorterDuff.Mode.SRC_ATOP
                )
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


