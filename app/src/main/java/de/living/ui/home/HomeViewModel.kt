package de.living.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Your Name"
    }
    val text: LiveData<String> = _text
}