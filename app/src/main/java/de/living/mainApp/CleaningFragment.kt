package de.living.mainApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.living.databinding.FragmentCleaningBinding
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
        _userDataViewModel.getUserData()
        _binding = FragmentCleaningBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textCleaning
        _userDataViewModel.getUser().observe(viewLifecycleOwner) {
            textView.text = _userDataViewModel.getUser().value?.name
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}