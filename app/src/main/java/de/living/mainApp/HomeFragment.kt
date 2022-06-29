package de.living.mainApp

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
    private var selectedItem = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val _userDataViewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        val editTextViewName: EditText = binding.editTextViewYourName
        val editTextViewEmail: EditText = binding.editTextViewYourEmail
        _userDataViewModel.getUser().observe(viewLifecycleOwner) {
            editTextViewName.setText(_userDataViewModel.getUser().value?.name)
            editTextViewEmail.setText(_userDataViewModel.getUser().value?.email)
        }
        inflateRecyclerview()

        binding.YourNameEditButton.setOnClickListener {
            binding.YourNameEditButton.hideKeyboard()
            _userDataViewModel.updateUserName(editTextViewName.text.toString())
            _userDataViewModel.setUserName(editTextViewName.text.toString())
        }
        binding.YourEmailEditButton.setOnClickListener {
            binding.YourEmailEditButton.hideKeyboard()
            _userDataViewModel.updateUserEmail(editTextViewEmail.text.toString())
            _userDataViewModel.setUserEmail(editTextViewEmail.text.toString())
        }

        binding.createGroupButton.setOnClickListener {
            buttonEffect(binding.createGroupButton)
            val dialog =
                EditTextDialog.newInstance(text = "", hint = "Group Name", isMultiline = false)
            dialog.onOk = {
                _userDataViewModel.createGroup(dialog.editText.text)
                _userDataViewModel.getUser().value?.let { it1 ->
                    _userDataViewModel.createTask("test",
                        it1.name,dialog.editText.text.toString())
                }
                inflateRecyclerview()
            }
            dialog.show(parentFragmentManager, "editDescription")
        }
        binding.leaveButton.setOnClickListener {
            if (selectedItem != 0) {
                buttonEffect(binding.leaveButton)
                _userDataViewModel.leaveGroup(
                    _userDataViewModel.getGroups().value?.groupNames?.get(
                        selectedItem
                    )
                )
                _userDataViewModel.getGroups().value?.groupNames?.removeAt(selectedItem)
                inflateRecyclerview()
            } else {
                Toast.makeText(activity, "No group selected", Toast.LENGTH_SHORT).show()
            }
            selectedItem = 0
        }
        binding.inviteButton.setOnClickListener {
            buttonEffect(binding.inviteButton)
            if (selectedItem != 0) {
                val dialog =
                    EditTextDialog.newInstance(text = "", hint = "E-Mail", isMultiline = false)
                dialog.onOk = {
                    _userDataViewModel.getGroups().value?.groupNames?.let { it1 ->
                        _userDataViewModel.addToGroup(
                            it1[selectedItem], dialog.editText.text
                        )
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


    private fun inflateRecyclerview() {
        // getting the recyclerview by its id
        val recyclerview = binding.listViewGroups
        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(binding.listViewGroups.context)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<String>()
        for (item in _userDataViewModel.getGroups().value?.groupNames!!) {
            _userDataViewModel.getGroups().value!!.groupNames?.let { data.add(item) }
        }

        // This will pass the ArrayList to our Adapter
        val adapter = AdapterRecyclerViewGroups(data)
        adapter.setOnItemClickListener(object : AdapterRecyclerViewGroups.OnItemClickListener {
            override fun setOnClickListener(pos: Int) {
                if (recyclerview.getChildAt(pos).isActivated) {
                    recyclerview.getChildAt(pos).isActivated = false
                    selectedItem = 0
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
