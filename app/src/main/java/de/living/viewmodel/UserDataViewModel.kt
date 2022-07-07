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
import com.google.firebase.firestore.FieldValue.arrayRemove
import com.google.firebase.firestore.FieldValue.arrayUnion
import com.google.firebase.firestore.FirebaseFirestore
import de.living.model.BigUser
import java.time.Instant
import java.time.temporal.ChronoUnit

@Suppress("unused")
class UserDataViewModel : ViewModel() {
    private val mFireStore = FirebaseFirestore.getInstance()
    var bigUser: MutableLiveData<BigUser> = MutableLiveData<BigUser>(BigUser())


    fun setUserName(s: String) {
        bigUser.value?.name = s
    }

    fun setUserEmail(email: String) {
        bigUser.value?.email = email
    }

    private fun setTasks(groupName: String, tasks: HashMap<String, String>) {
        bigUser.value?.tasksPerGroup?.get(groupName)?.add(tasks)
    }

    private fun setGroup(s: Editable) {
        bigUser.value?.let { bigUser.value?.memberPerGroup?.get(s.toString())?.add(it.name) }
    }

    fun getTasks(s: String): ArrayList<HashMap<String, String>>? {
        return bigUser.value?.tasksPerGroup?.get(s)
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    private fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser!!.email
    }

    fun getUser(): LiveData<BigUser> {
        return bigUser
    }

    fun getGroups(): ArrayList<String>? {
        return bigUser.value?.groupNames
    }

    fun getGroupMemberNames(groupName: String): java.util.ArrayList<String>? {
        return bigUser.value?.memberPerGroup?.get(groupName)
    }


    fun getTasksFromDatabase() {
        val docRef = mFireStore.collection("groups")
            .whereArrayContains("user", bigUser.value?.email.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                Log.d(
                    TAG,
                    "Tasks from  Database Snapshot (size=${document.size()}):  ${document.documents}"
                )
                if (!document.isEmpty) {
                    bigUser.value?.groupNames?.clear()
                    for (doc in document) {
                        bigUser.value?.groupNames?.add(doc.id)
                        val test = doc.data["user"] as  ArrayList<String>
                        bigUser.value?.memberPerGroup?.set(doc.id, test)
                        if(doc.data["tasks"] != null) {
                            bigUser.value?.tasksPerGroup?.put(
                                doc.id,
                                doc.data["tasks"] as ArrayList<HashMap<String, String>>
                            )
                        }
                    }
                }
            }
    }

    fun getUserData() {
        val docRef = getCurrentUserEmail()?.let { mFireStore.collection("users").document(it) }
        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                Log.d(TAG, "getUserData snapshot name: ${document.data?.get("name")}")
                bigUser.value?.name = document.data?.get("name") as String
                bigUser.value?.email = document.data?.get("email") as String
                bigUser.value?.uid = document.data?.get("uid") as String
            } else {
                Log.d(TAG, "No such document")
            }
        }?.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }


    fun updateUserName(string: String) {
        val docRef = getCurrentUserEmail()?.let { mFireStore.collection("users").document(it) }
        docRef?.update("name", string)?.addOnSuccessListener { Log.d(TAG, "Username successfully updated") }
            ?.addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    fun updateUserEmail(string: String) {
        val docRef = mFireStore.collection("users").document(getCurrentUserId())
        docRef
            .update("email", string)
            .addOnSuccessListener { Log.d(TAG, "Email successfully updated") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }


    fun addToGroup(groupName: String, emailToAdd: String) {
        mFireStore.collection("groups").document(groupName)
            .update("user", arrayUnion(emailToAdd))
            .addOnSuccessListener {
                Log.d(TAG, "User successfully to group users added")
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }



    fun createTask(task: String, memberName: String, groupName: String) {
        val seconds: Long = Timestamp.now().seconds
        val addedSeconds = Instant.ofEpochSecond(seconds).plus(7, ChronoUnit.DAYS).epochSecond
        val newTimeStamp = Timestamp(addedSeconds, 0)
        val mapOfTask = hashMapOf(
            "name" to task,
            "memberToDo" to memberName,
            "timeCreated" to Timestamp.now(),
            "timeDeadline" to newTimeStamp
        )

        mFireStore.collection("groups").document(groupName)
            .update("tasks", arrayUnion(mapOfTask))
            .addOnSuccessListener {
                setTasks(groupName, mapOfTask as HashMap<String, String>)
                Log.d(TAG, "Task successfully created")
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun createGroup(s: String) {
        val docData: MutableMap<String, Any> = HashMap()
        docData["user"] = listOf(getUser().value?.email)
        mFireStore.collection("groups").document(s)
            .set(docData)
            .addOnSuccessListener { Log.d(TAG, "Group to groups successfully created") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    fun leaveGroup(s: String?) {
        if (s != null) {
            mFireStore.collection("groups").document(s)
                .update("user", arrayRemove(getUser().value?.email))
                .addOnSuccessListener { Log.d(TAG, "User successfully removed from groups/user") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }
    }

    fun deleteTask(index: Int,group: String){
        mFireStore.collection("groups").document(group)
            .update("tasks", arrayRemove(getTasks(group)?.get(index)))
            .addOnSuccessListener { Log.d(TAG, "Member of task successfully deleted") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

        getTasks(group)?.removeAt(index)
    }

    fun markTaskAsFinished(index: Int, group: String) {

        val memberOnTask = getTasks(group)?.get(index)?.get("memberToDo")
        var indexOfNext =
            getGroupMemberNames(group)?.indexOf(memberOnTask)?.plus(1)
        if (indexOfNext != null) {
            if(indexOfNext >= getGroupMemberNames(group)?.size!!){
                indexOfNext = 0
            }
        }
        val nextName = indexOfNext?.let { bigUser.value?.memberPerGroup?.get(group)?.get(it) }


        mFireStore.collection("groups").document(group)
            .update("tasks", arrayRemove(getTasks(group)?.get(index)))
            .addOnSuccessListener { Log.d(TAG, "Member of task successfully deleted") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        if (nextName != null) {
            getTasks(group)?.get(index)?.put("memberToDo",nextName)
        }
        if (nextName != null) {
            val seconds1: Long = Timestamp.now().seconds
            val addedSeconds1 = Instant.ofEpochSecond(seconds1).plus(7, ChronoUnit.DAYS).epochSecond
            val newTimeStamp1 = Timestamp(addedSeconds1, 0)
            val mapOfTask = hashMapOf(
                "name" to (getTasks(group)?.get(index)?.get("name")),
                "memberToDo" to nextName,
                "timeCreated" to Timestamp.now(),
                "timeDeadline" to newTimeStamp1
            )

            mFireStore.collection("groups").document(group)
                .update("tasks", arrayUnion(mapOfTask))
                    .addOnSuccessListener { Log.d(TAG, "Member of task successfully updated") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }



    }

}
