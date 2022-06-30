package de.living.mainApp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.living.R
import de.living.databinding.ActivityMainBinding
import de.living.viewmodel.UserDataViewModel
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model: UserDataViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
        val job = CoroutineScope(Dispatchers.IO).launchPeriodicAsync(2000) {
            init()
        }

        job.start()
    }


    private fun CoroutineScope.launchPeriodicAsync(
        repeatMillis: Long,
        action: () -> Unit
    ) = this.async {
        if (repeatMillis > 0) {
            while (isActive) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }
    private fun init() {
        model.getTasksFromDatabase(Firebase.auth.currentUser?.email+"ownGroup")
        model.getUserGroups()
        model.getUserData()

    }


}