package com.example.globetrotter.ui.placesDetail.peopleVisits

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.data.Users
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Story
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PeopleVisitsViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = Firebase.firestore

    private val _visitedUserProfileImages = MutableLiveData<List<String>>()
    val visitedUserProfileImages: LiveData<List<String>> = _visitedUserProfileImages

    private val _peopleList = MutableLiveData<Resource<List<Pair<Users, Boolean>>>>()
    val peopleList: LiveData<Resource<List<Pair<Users, Boolean>>>> = _peopleList

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
        val userDetails = mutableListOf<Pair<Users, Boolean>>()
        val userFetchTasks = mutableListOf<Task<DocumentSnapshot>>()

        userIds.forEach { userId ->
            val userTask = firestore.collection("Users").document(userId).get()
            userFetchTasks.add(userTask)
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(userFetchTasks).addOnSuccessListener {
            val storyFetchTasks = mutableListOf<Task<Boolean>>()

            userFetchTasks.forEach { task ->
                val document = task.result
                val user = document.toUser()
                user?.let {
                    storyFetchTasks.add(fetchStoriesForUser(it.userId))
                    userDetails.add(Pair(it, false))
                }
            }

            Tasks.whenAllSuccess<Boolean>(storyFetchTasks).addOnSuccessListener { storyResults ->
                storyResults.forEachIndexed { index, hasStory ->
                    userDetails[index] = Pair(userDetails[index].first, hasStory)
                }
                _peopleList.value = Resource.Success(userDetails)
            }.addOnFailureListener { exception ->
                Log.e("PeopleVisitsViewModel", "Error fetching stories: $exception")
                _peopleList.value = Resource.Error(exception)
            }
        }.addOnFailureListener { exception ->
            Log.e("PeopleVisitsViewModel", "Error getting user details: $exception")
            _peopleList.value = Resource.Error(exception)
        }
    }

    private fun fetchStoriesForUser(userId: String): Task<Boolean> {
        val taskCompletionSource = com.google.android.gms.tasks.TaskCompletionSource<Boolean>()

        firestore.collection("Story")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val hasStory = querySnapshot.documents.isNotEmpty()
                taskCompletionSource.setResult(hasStory)
            }.addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
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
