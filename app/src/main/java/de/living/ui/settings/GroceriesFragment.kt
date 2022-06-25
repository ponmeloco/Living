package de.living.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.living.databinding.FragmentGroceriesBinding
import de.living.startup.IntroActivity

class GroceriesFragment : Fragment() {

    private var _binding: FragmentGroceriesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this)[GroceriesViewModel::class.java]

        _binding = FragmentGroceriesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton: Button = binding.logoutButton
        val auth = Firebase.auth
        logoutButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(activity)

            alertDialog.apply {
                setMessage("Wanted to logout?")
                setPositiveButton("Yes!") { _, _ ->
                    if(auth.currentUser != null)run {
                        auth.signOut()
                        startActivity(Intent(activity, IntroActivity::class.java))
                        activity?.finishAffinity()
                    }
                }
                setNegativeButton("No!") { _, _ ->

                }
            }.create().show()


        }
        val textView: TextView = binding.textGroceries
        settingsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}