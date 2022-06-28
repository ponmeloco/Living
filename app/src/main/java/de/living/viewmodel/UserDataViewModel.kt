package de.living.viewmodel

import android.content.ContentValues
import android.content.ContentValues.TAG
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

    fun setUserName(string: String){
        _user.value?.name  = string
    }
    fun setUserEmail(string: String){
        _user.value?.email  = string
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
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    _user = MutableLiveData(document.toObject<User>()!!)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun getUserGroups() {
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
            .collection("groups")
            .document("groupNames")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    _userGroupsList = MutableLiveData(document.toObject<GroupsList>()!!)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun updateUserName(string: String){
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
        docRef
            .update("name", string)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    fun updateUserEmail(string: String){
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
        docRef
            .update("email", string)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }



    fun init(){
        getUserGroups()
        getUserData()
    }


}
