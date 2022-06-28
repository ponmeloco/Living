package de.living.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import de.living.model.GroupsList
import de.living.model.User

class UserDataViewModel : ViewModel() {
    private val mFireStore = FirebaseFirestore.getInstance()
    private var _user: MutableLiveData<User> =  MutableLiveData<User>()
    private var _userGroupsList: MutableLiveData<GroupsList> =   MutableLiveData<GroupsList>()

    fun getUser(): LiveData<User> {
        return _user
    }

    fun getGroups(): LiveData<GroupsList> {
        return _userGroupsList
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    private fun getUserData() {
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                    _user = MutableLiveData(document.toObject<User>()!!)
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    private fun getUserGroups() {
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
            .collection("groups")
            .document("groupNames")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                    _userGroupsList = MutableLiveData(document.toObject<GroupsList>()!!)
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    fun init(){
        getUserGroups()
        getUserData()
    }


}
