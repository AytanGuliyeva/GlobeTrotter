package com.example.globetrotter.ui.placesDetail.peopleVisits

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.data.Users
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Story
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

    /* private val _peopleList = MutableLiveData<Resource<List<Pair<Users, List<Story>>>>>()
     val peopleList: LiveData<Resource<List<Pair<Users, List<Story>>>>>
         get() = _peopleList
    */
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
    /*
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
    */

    /* private fun fetchUserDetails(userIds: List<String>) {
         val userDetails = mutableListOf<Pair<Users, List<Story>>>()
         val userFetchTasks = mutableListOf<com.google.android.gms.tasks.Task<DocumentSnapshot>>()

         for (userId in userIds) {
             val userTask = firestore.collection("Users").document(userId).get()
             userFetchTasks.add(userTask)
         }

         Tasks.whenAllSuccess<DocumentSnapshot>(userFetchTasks).addOnSuccessListener {
             for (task in userFetchTasks) {
                 val document = task.result
                 val user = document.toUser()
                 val stories = fetchStoriesForUser(user?.userId)

                 user?.let {
                     userDetails.add(Pair(it, stories))
                 }
             }
             _peopleList.value = Resource.Success(userDetails)
         }.addOnFailureListener { exception ->
             Log.e("PeopleVisitsViewModel", "Error getting user details: $exception")
             _peopleList.value = Resource.Error(exception)
         }
     }*/
//tekce user adlari
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

    private fun fetchStoriesForUser(userId: String?): List<Story> {
        val stories = mutableListOf<Story>()
        firestore.collection("Story")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val story = document.toStory()
                    if (story != null) {
                        stories.add(story)
                    }
                }
            }
        return stories
    }

    private fun DocumentSnapshot.toStory(): Story? {
        return try {
            val storyId = getString("storyId") ?: return null
            val userId = getString("userId") ?: return null
            val imageUrl = getString("imageUrl") ?: return null
            val timeStart = getLong("timeStart") ?: 0
            val timeEnd = getLong("timeEnd") ?: 0
            val caption = getString("caption") ?: ""

            Story(
                storyId = storyId,
                userId = userId,
                imageUrl = imageUrl,
                timeStart = timeStart,
                timeEnd = timeEnd,
                caption = caption
            )
        } catch (e: Exception) {
            null
        }
    }


    /*
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
    */

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
