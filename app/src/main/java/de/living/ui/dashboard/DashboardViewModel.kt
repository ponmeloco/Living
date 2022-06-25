package de.living.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = FirebaseAuth.getInstance().currentUser?.uid
    }
    val text: LiveData<String> = _text
}