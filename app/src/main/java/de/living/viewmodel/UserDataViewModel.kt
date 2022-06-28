package de.living.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import de.living.model.GroupsList
import de.living.model.GroupsNamesList
import de.living.model.User
import kotlin.collections.HashMap

class UserDataViewModel : ViewModel() {
    private val mFireStore = FirebaseFirestore.getInstance()
    private var _user: MutableLiveData<User> =  MutableLiveData<User>()
    private var _userUIDtoName: MutableLiveData<User> =  MutableLiveData<User>()
    private var _groupsNamesList: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()
    private var _userGroupsList: MutableLiveData<GroupsList> =   MutableLiveData<GroupsList>()
    private var _groupsNamesUid: MutableLiveData<GroupsNamesList> = MutableLiveData<GroupsNamesList>()

    fun setUserName(string: String){
        _user.value?.name  = string
    }
    fun setUserEmail(string: String){
        _user.value?.email  = string
    }

    fun getGroupMemberNames(): LiveData<ArrayList<String>>{
        return _groupsNamesList
    }

    fun getUser(): LiveData<User> {
        return _user
    }

    fun getGroups(): LiveData<GroupsList> {
        return _userGroupsList
    }

    private fun setGroup(s: String){
        _userGroupsList.value?.group?.add(s)
    }

    fun getGroupMemberUID(s: String){
        val docRef = mFireStore.collection("groups").document(s)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    _groupsNamesUid = MutableLiveData(document.toObject<GroupsNamesList>()!!)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
    fun leaveGroup(s: String?){
        if (s != null) {
                mFireStore.collection("groups").document(s)
                    .update("User", FieldValue.arrayRemove(getUser().value?.uid)).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                mFireStore.collection("users").document(getCurrentUserId()).collection("groups").document("groupNames")
                    .update("group", FieldValue.arrayRemove(s)).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }

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

    fun createGroup(s: String) {
        val docData: MutableMap<String, Any> = HashMap()
        docData["user"] = listOf(getUser().value?.uid)

        mFireStore.collection("users").document(getCurrentUserId()).collection("groups").document("groupNames")
            .update("group", FieldValue.arrayUnion(s)).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        mFireStore.collection("groups").document(s)
            .set(docData).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        setGroup(s)
    }

    fun uidToUsernames(){
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
      for (item in _groupsNamesUid.value?.user!!) {
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    _userUIDtoName = MutableLiveData(document.toObject<User>()!!)
                    _userUIDtoName.value?.let { _groupsNamesList.value?.add(it.name) }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        }
    }

}
