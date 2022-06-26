package de.living.mainApp

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.living.databinding.FragmentHomeBinding
import de.living.viewmodel.UserDataViewModel

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

    ): ConstraintLayout {



        _binding = FragmentHomeBinding.inflate(inflater, container, false)


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

}
