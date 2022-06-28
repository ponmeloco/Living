package de.living.viewmodel

import android.content.ContentValues.TAG
import android.text.Editable
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

@Suppress("unused")
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

    fun getUser(): LiveData<User> {
        return _user
    }

    fun getGroups(): LiveData<GroupsList> {
        return _userGroupsList
    }

    private fun setGroup(s: Editable){
        _userGroupsList.value?.groupNames?.add(s.toString())
    }


    fun leaveGroup(s: String?){
        if (s != null) {
                mFireStore.collection("groups").document(s)
                    .update("user", FieldValue.arrayRemove(getUser().value?.email)).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            getCurrentUserEmail()?.let {
                mFireStore.collection("users").document(it).collection("groups").document("groupNames")
                    .update("groupNames", FieldValue.arrayRemove(s)).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            }
        }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    private fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser!!.email
    }


    private fun getUserData() {
        val docRef = getCurrentUserEmail()?.let { mFireStore.collection("users").document(it) }
        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                _user = MutableLiveData(document.toObject<User>()!!)
            } else {
                Log.d(TAG, "No such document")
            }
        }?.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    private fun getUserGroups() {

        val docRef = getCurrentUserEmail()?.let {
            mFireStore.collection("users").document(it)
                .collection("groups")
                .document("groupNames")
        }
        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                _userGroupsList = MutableLiveData(document.toObject<GroupsList>()!!)
            } else {
                Log.d(TAG, "No such document")
            }
        }?.addOnFailureListener { exception ->
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


    fun addToGroup(groupName: String, emailToAdd: Editable){
        mFireStore.collection("groups").document(groupName)
            .update("user", FieldValue.arrayUnion(emailToAdd.toString().lowercase())).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        mFireStore.collection("users").document(emailToAdd.toString()).collection("groups").document("groupNames")
            .update("groupNames", FieldValue.arrayUnion(groupName)).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

    }



    fun createGroup(s: Editable) {
        val docData: MutableMap<String, Any> = HashMap()
        docData["user"] = listOf(getUser().value?.email)

        getCurrentUserEmail()?.let {
            mFireStore.collection("users").document(it).collection("groups").document("groupNames")
                .update("groupNames", FieldValue.arrayUnion(s.toString())).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }
        mFireStore.collection("groups").document(s.toString())
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

    fun getGroupMemberNames(): LiveData<ArrayList<String>>{
        return _groupsNamesList
    }
}
