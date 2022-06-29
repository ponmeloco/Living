@file:Suppress("UNCHECKED_CAST")

package de.living.viewmodel

import android.content.ContentValues.TAG
import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldValue.arrayUnion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import de.living.model.GroupsList
import de.living.model.GroupsNamesList
import de.living.model.Tasks
import de.living.model.User
import java.util.*

@Suppress("unused")
class UserDataViewModel : ViewModel() {
    private lateinit var _tasks: MutableLiveData<Tasks>
    private val mFireStore = FirebaseFirestore.getInstance()
    private var _user: MutableLiveData<User> = MutableLiveData<User>()
    private var _userUIDtoName: MutableLiveData<User> = MutableLiveData<User>()
    private var _groupsNamesList: MutableLiveData<ArrayList<String>> =
        MutableLiveData<ArrayList<String>>()
    private var _userGroupsList: MutableLiveData<GroupsList> = MutableLiveData<GroupsList>()
    private var _groupsNamesUid: MutableLiveData<GroupsNamesList> =
        MutableLiveData<GroupsNamesList>()

    fun setUserName(string: String) {
        _user.value?.name = string
    }

    fun setUserEmail(string: String) {
        _user.value?.email = string
    }

    fun getUser(): LiveData<User> {
        return _user
    }

    fun getGroups(): LiveData<GroupsList> {
        return _userGroupsList
    }

    private fun setGroup(s: Editable) {
        _userGroupsList.value?.groupNames?.add(s.toString())
    }

    fun getTasks(): MutableLiveData<Tasks> {
        return _tasks

    }


    fun getTasksFromDatabase(group: String) {
        val docRef = mFireStore.collection("groups").document(group)
        docRef.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "Task from Database $group ${document.get("tasks")}")
                _tasks =
                    MutableLiveData(Tasks(document.get("tasks") as ArrayList<HashMap<String, String>>))
                Log.d(TAG, "GET TASK FROM DATABASE CALLED")
            }
    }

    fun getUserGroups() {
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

    fun leaveGroup(s: String?) {
        if (s != null) {
            mFireStore.collection("groups").document(s)
                .update("user", FieldValue.arrayRemove(getUser().value?.email))
                .addOnSuccessListener { Log.d(TAG, "User successfully removed from groups/user") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            getCurrentUserEmail()?.let {
                mFireStore.collection("users").document(it).collection("groups")
                    .document("groupNames")
                    .update("groupNames", FieldValue.arrayRemove(s))
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "User successfully removed from user/groupNames"
                        )
                    }
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


    fun getUserData() {
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


    fun updateUserName(string: String) {
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
        docRef
            .update("name", string)
            .addOnSuccessListener { Log.d(TAG, "Username successfully updated") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    fun updateUserEmail(string: String) {
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
        docRef
            .update("email", string)
            .addOnSuccessListener { Log.d(TAG, "Email successfully updated") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }


    fun addToGroup(groupName: String, emailToAdd: Editable) {
        mFireStore.collection("groups").document(groupName)
            .update("user", arrayUnion(emailToAdd.toString().lowercase()))
            .addOnSuccessListener { Log.d(TAG, "User successfully to group users added") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        mFireStore.collection("users").document(emailToAdd.toString()).collection("groups")
            .document("groupNames")
            .update("groupNames", arrayUnion(groupName))
            .addOnSuccessListener { Log.d(TAG, "User successfully invited to group") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

    }

    fun createTask(_task: String, _memberName: String, s: String) {
        val mapOfTask = hashMapOf(
            "name" to _task,
            "memberToDo" to _memberName,
            "timeCreated" to Timestamp.now()
        )

        mFireStore.collection("groups").document(s)
            .update("tasks", arrayUnion(mapOfTask))
            .addOnSuccessListener { Log.d(TAG, "Task successfully created") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun createGroup(s: Editable) {
        val docData: MutableMap<String, Any> = HashMap()
        docData["user"] = listOf(getUser().value?.email)

        getCurrentUserEmail()?.let {
            mFireStore.collection("users").document(it).collection("groups").document("groupNames")
                .update("groupNames", arrayUnion(s.toString()))
                .addOnSuccessListener { Log.d(TAG, "Group to user/groups successfully created") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }
        mFireStore.collection("groups").document(s.toString())
            .set(docData)
            .addOnSuccessListener { Log.d(TAG, "Group to groups successfully created") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        setGroup(s)

        val nestedData = hashMapOf(
            "name" to "Should",
            "memberToDo" to "something",
            "timeCreated" to Timestamp(Date())
        )
        val docData2 = hashMapOf(
            "tasks" to arrayListOf(nestedData),
        )
        mFireStore.collection("groups").document(s.toString())
            .set(docData2)
            .addOnSuccessListener { Log.d(TAG, "Task successfully created") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

    }

    fun uidToUsernames() {
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

    fun getGroupMemberUID(s: String) {
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

    fun getGroupMemberNames(): LiveData<ArrayList<String>> {
        return _groupsNamesList
    }
}
