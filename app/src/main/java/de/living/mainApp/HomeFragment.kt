package de.living.mainApp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.living.EditTextDialog
import de.living.adapter.AdapterRecyclerViewGroups
import de.living.databinding.FragmentHomeBinding
import de.living.viewmodel.UserDataViewModel


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private var selectedItem = -1
    private lateinit var adapter: AdapterRecyclerViewGroups

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // getting the recyclerview by its id
        val recyclerview = binding.listViewGroups
        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(binding.listViewGroups.context)
        // ArrayList of class ItemsViewModel
        val data = ArrayList<String>()
        viewModel.bigUser.value?.groupNames.let {
            if (it != null) {
                data.addAll(it)
            }
        }
        // This will pass the ArrayList to our Adapter
        adapter = AdapterRecyclerViewGroups(data)
        adapter.setOnItemClickListener(object : AdapterRecyclerViewGroups.OnItemClickListener {
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
        recyclerview.adapter = adapter

        val editTextViewName: EditText = binding.editTextViewYourName
        val editTextViewEmail: EditText = binding.editTextViewYourEmail

        viewModel.bigUser.observe(viewLifecycleOwner) {
            editTextViewName.setText(it.name)
            editTextViewEmail.setText(it.email)
        }

        binding.YourNameEditButton.setOnClickListener {
            binding.YourNameEditButton.hideKeyboard()
            binding.editTextViewYourName.isEnabled = !binding.editTextViewYourName.isEnabled
        }

        binding.YourEmailEditButton.setOnClickListener {
            binding.YourEmailEditButton.hideKeyboard()
            binding.editTextViewYourName.isEnabled = !binding.editTextViewYourName.isEnabled
            viewModel.setUserName(editTextViewName.text.toString())
            viewModel.updateUserName(editTextViewName.text.toString())
            Toast.makeText(activity, "New name set!", Toast.LENGTH_SHORT).show()
        }

        binding.createGroupButton.setOnClickListener {
            buttonEffect(binding.createGroupButton)
            val dialog =
                EditTextDialog.newInstance(text = "", hint = "Group Name", isMultiline = false)
            dialog.onOk = {
                viewModel.createGroup(dialog.editText.text.toString())
                data.add(dialog.editText.text.toString())
                adapter.notifyDataSetChanged()
            }
            dialog.show(parentFragmentManager, "editDescription")
        }

        binding.leaveButton.setOnClickListener {
            buttonEffect(binding.leaveButton)
            if (selectedItem != -1) {
                val alertDialog = AlertDialog.Builder(activity)
                alertDialog.apply {
                    setMessage("Do you want to leave the group?")
                    setPositiveButton("Yes!") { _, _ ->
                        viewModel.leaveGroup(viewModel.getGroups()?.get(selectedItem))
                        if (selectedItem != -1) {
                            viewModel.getGroups()?.removeAt(selectedItem)
                            data.removeAt(selectedItem)
                            adapter.notifyDataSetChanged()
                            selectedItem = -1
                        }
                    }
                    setNegativeButton("No!") { _, _ ->
                    }
                }.create().show()
            } else {
                Toast.makeText(activity, "No group selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.inviteButton.setOnClickListener {
            buttonEffect(binding.inviteButton)
            if (selectedItem != -1) {
                val dialog =
                    EditTextDialog.newInstance(text = "", hint = "E-Mail", isMultiline = false)
                dialog.onOk = {
                    viewModel.getGroups().let { it1 ->
                        it1?.get(selectedItem)?.let { it2 ->
                            viewModel.addToGroup(
                                it2, dialog.editText.text.toString()
                            )
                        }
                    }
                }
                dialog.show(parentFragmentManager, "editDescription")
            } else {
                Toast.makeText(activity, "No group selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pendingInvitesButton.setOnClickListener {
            buttonEffect(binding.pendingInvitesButton)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
