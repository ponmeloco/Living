package de.living.mainApp

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.living.databinding.FragmentHomeBinding
import de.living.viewmodel.UserDataViewModel
import de.living.adapter.MyAdapter
import de.living.model.GroupsList


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val _userDataViewModel: UserDataViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {

        _userDataViewModel.getUserGroups()
        _userDataViewModel.getUserData()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        val editTextViewName: EditText = binding.editTextViewYourName
        val editTextViewEmail: EditText = binding.editTextViewYourEmail
        _userDataViewModel.getUser().observe(viewLifecycleOwner) {
            editTextViewName.setText(_userDataViewModel.getUser().value?.name)
            editTextViewEmail.setText(_userDataViewModel.getUser().value?.email)
        }


        inflateRecyclerview()


        binding.createGroupButton.setOnClickListener {
            buttonEffect(binding.createGroupButton)
        }
        binding.leaveButton.setOnClickListener {
            buttonEffect(binding.leaveButton)
        }
        binding.inviteButton.setOnClickListener {
            buttonEffect(binding.inviteButton)
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

     private fun inflateRecyclerview(){
        // getting the recyclerview by its id
        val recyclerview = binding.listViewGroups

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(binding.listViewGroups.context)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<GroupsList>()

         _userDataViewModel.getGroups().value?.let { GroupsList(it.Gruppe1) }?.let { data.add(it) }

        // This will pass the ArrayList to our Adapter
        val adapter = MyAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
    }

}
