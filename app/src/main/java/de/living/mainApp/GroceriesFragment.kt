@file:Suppress("unused")

package de.living.mainApp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.living.databinding.FragmentGroceriesBinding
import de.living.startup.IntroActivity
import de.living.viewmodel.UserDataViewModel

class GroceriesFragment : Fragment() {

    private var _binding: FragmentGroceriesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val _userDataViewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGroceriesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton: Button = binding.logoutButton


        logoutButton.setOnClickListener {
            logout()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        val alertDialog = AlertDialog.Builder(activity)
        val auth = Firebase.auth
        alertDialog.apply {
            setMessage("Wanted to logout?")
            setPositiveButton("Yes!") { _, _ ->
                if (auth.currentUser != null) run {
                    auth.signOut()
                    startActivity(Intent(activity, IntroActivity::class.java))
                    activity?.finishAffinity()
                }
            }
            setNegativeButton("No!") { _, _ ->
            }
        }.create().show()

    }
}