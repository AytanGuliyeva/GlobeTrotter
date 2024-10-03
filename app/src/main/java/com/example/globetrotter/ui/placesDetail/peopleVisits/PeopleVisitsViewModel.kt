package com.example.globetrotter.ui.placesDetail.peopleVisits

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.data.Users
import com.example.globetrotter.base.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PeopleVisitsViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = Firebase.firestore

    private val _visitedUserProfileImages = MutableLiveData<List<String>>()
    val visitedUserProfileImages: LiveData<List<String>>
        get() = _visitedUserProfileImages

    private val _peopleList = MutableLiveData<Resource<List<Users>>>()
    val peopleList: LiveData<Resource<List<Users>>>
        get() = _peopleList

    fun fetchVisitedPeople(placesId: String) {
        firestore.collection("Visited").document(placesId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    val peoples = documentSnapshot.data
                    if (peoples != null) {
                        val userIds = peoples.keys.toList()
                        fetchUserDetails(userIds)
                    } else {
                        _peopleList.value = Resource.Error(Exception("No visited users found"))
                    }
                } catch (e: Exception) {
                    _peopleList.value = Resource.Error(e)
                }
            }
            .addOnFailureListener { exception ->
                _peopleList.value = Resource.Error(exception)
            }
    }

    private fun fetchUserDetails(userIds: List<String>) {
        val userDetails = mutableListOf<Users>()
        val userFetchTasks = mutableListOf<com.google.android.gms.tasks.Task<DocumentSnapshot>>()

        for (userId in userIds) {
            val userTask = firestore.collection("Users").document(userId).get()
            userFetchTasks.add(userTask)
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(userFetchTasks).addOnSuccessListener {
            for (task in userFetchTasks) {
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
            val userId = getString(ConstValues.USER_ID)
            val username = getString(ConstValues.USERNAME)
            val email = getString(ConstValues.EMAIL)
            val password = getString(ConstValues.PASSWORD)
            val imageUrl = getString(ConstValues.IMAGE_URL)

            Users(
                userId.orEmpty(),
                username.orEmpty(),
                email.orEmpty(),
                password.orEmpty(),
                imageUrl.orEmpty(),
            )
        } catch (e: Exception) {
            null
        }
    }
}
