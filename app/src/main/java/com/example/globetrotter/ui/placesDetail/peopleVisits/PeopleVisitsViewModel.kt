package com.example.globetrotter.ui.placesDetail.peopleVisits

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.data.Users
import com.example.globetrotter.base.Resource
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PeopleVisitsViewModel @Inject constructor(
    val firestore: FirebaseFirestore
) : ViewModel() {
   // private val firestore: FirebaseFirestore = Firebase.firestore

    private val _peopleList = MutableLiveData<Resource<List<Users>>>()
    val peopleList: LiveData<Resource<List<Users>>> = _peopleList

    fun fetchVisitedPeople(placesId: String) {
        firestore.collection("Visited").document(placesId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val peoples = documentSnapshot.data
                if (peoples != null) {
                    val userIds = peoples.keys.toList()
                    fetchUserDetails(userIds)
                } else {
                    _peopleList.value = Resource.Error(Exception("No visited users found"))
                }
            }
            .addOnFailureListener { exception ->
                _peopleList.value = Resource.Error(exception)
            }
    }

    private fun fetchUserDetails(userIds: List<String>) {
        val userDetails = mutableListOf<Users>()
        val userFetchTasks = mutableListOf<Task<DocumentSnapshot>>()

        userIds.forEach { userId ->
            val userTask = firestore.collection("Users").document(userId).get()
            userFetchTasks.add(userTask)
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(userFetchTasks).addOnSuccessListener {
            userFetchTasks.forEach { task ->
                val document = task.result
                val user = document.toUser()
                user?.let {
                    userDetails.add(it)
                }
            }
            _peopleList.value = Resource.Success(userDetails)
        }.addOnFailureListener { exception ->
            Log.e("PeopleVisitsViewModel", "Error getting user details: $exception")
            _peopleList.value = Resource.Error(exception)
        }
    }

    private fun DocumentSnapshot.toUser(): Users? {
        return try {
            val userId = getString(ConstValues.USER_ID) ?: return null
            val username = getString(ConstValues.USERNAME) ?: return null
            val email = getString(ConstValues.EMAIL) ?: return null
            val password = getString(ConstValues.PASSWORD) ?: return null
            val imageUrl = getString(ConstValues.IMAGE_URL) ?: return null

            Users(userId, username, email, password, imageUrl)
        } catch (e: Exception) {
            null
        }
    }
}
