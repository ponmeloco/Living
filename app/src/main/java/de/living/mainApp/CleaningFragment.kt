package de.living.mainApp


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
import de.living.EditTextDialog
import de.living.adapter.AdapterRecyclerViewTasks
import de.living.databinding.FragmentCleaningBinding
import de.living.viewmodel.UserDataViewModel


class CleaningFragment : Fragment() {
    private var _binding: FragmentCleaningBinding? = null
    private var selectedItem: Int = 0

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

        _userDataViewModel.getGroups().observe(viewLifecycleOwner){
            inflateRecyclerview()
        }

        binding.markAsFinishedButton.setOnClickListener{
            if (selectedItem != 0) {
                buttonEffect(binding.markAsFinishedButton)
                 _userDataViewModel.markTaskAsFinished(selectedItem,binding.groupSpinner.selectedItem.toString())
            } else {
                Toast.makeText(activity, "No group selected", Toast.LENGTH_SHORT).show()
            }
        }


        binding.addButton.setOnClickListener {
            val dialog =
                EditTextDialog.newInstance(text = "", hint = "Task name", isMultiline = false)
            dialog.onOk = {
                _userDataViewModel.getUser().value?.let { it1 ->
                    _userDataViewModel.createTask(dialog.editText.text.toString(), it1.name,
                        (_userDataViewModel.getUser().value?.email) +"ownGroup")
                }
                inflateRecyclerview()
            }
            dialog.show(parentFragmentManager, "editDescription")
        }

        binding.groupSpinner.setSelection(0)
        binding.groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                _userDataViewModel.getTasksFromDatabase(binding.groupSpinner.getItemAtPosition(position).toString())
                binding.recyclerViewTasks.adapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun inflateRecyclerview() {
        // getting the recyclerview by its id
        val recyclerview = binding.recyclerViewTasks

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(binding.recyclerViewTasks.context)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<HashMap<String, String>>()

        for (item in _userDataViewModel.getTasks().value?.tasks!!)
            data.add(item)

        // This will pass the ArrayList to our Adapter
        val adapter = AdapterRecyclerViewTasks(data)

        adapter.setOnItemClickListener(object : AdapterRecyclerViewTasks.OnItemClickListener {
            override fun setOnClickListener(pos: Int) {
                if (recyclerview.getChildAt(pos).isActivated) {
                    recyclerview.getChildAt(pos).isActivated = false
                } else {
                    recyclerview.getChildAt(selectedItem).isActivated = false
                    selectedItem = pos
                    recyclerview.getChildAt(selectedItem).isActivated = true
                }
            }
        })

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
    }


    private fun inflateGroupSpinner() {
        val spinner = binding.groupSpinner
        val groupNames = _userDataViewModel.getGroups().value?.groupNames as ArrayList

        val adapter = this@CleaningFragment.context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_item, groupNames
            )
        }
        spinner.adapter = adapter



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

