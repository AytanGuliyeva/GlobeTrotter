package com.example.globetrotter.ui.placesDetail.peopleVisits.overView

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.data.Story
import com.example.globetrotter.data.Users
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OverViewViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private var placesList: List<Places> = emptyList()

    private val _placesResult = MutableLiveData<Resource<Places>>()
    val placesResult: LiveData<Resource<Places>> get() = _placesResult


    private val storyList = ArrayList<Story>()
    private val _storyInformation = MutableLiveData<Resource<List<Story>>>()
    val storyInformation: LiveData<Resource<List<Story>>>
        get() = _storyInformation

    fun getStories(userId: String, placesId: String) {
        val ref = firestore.collection("Story").document(userId)
        ref.get().addOnSuccessListener { value ->
            if (value != null && value.exists()) {
                storyList.clear()
                try {
                    val doc = value.data as HashMap<*, *>
                    for (i in doc) {
                        val story = i.value as HashMap<*, *>
                        val imageurl = story[ConstValues.IMAGE_URL] as String
                        val storyId = story["storyId"] as String
                        val caption = story["caption"] as String
                        val storyPlacesId = story["placesId"] as String

                        if (storyPlacesId == placesId) {
                            val storyi = Story(
                                imageUrl = imageurl,
                                storyId = storyId,
                                caption = caption,
                                placesId = storyPlacesId
                            )
                            storyList.add(storyi)
                        }
                    }
                    if (storyList.isNotEmpty()) {
                        _storyInformation.postValue(Resource.Success(storyList))
                    } else {
                        _storyInformation.postValue(Resource.Error(Exception("No stories available for this place")))
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    _storyInformation.postValue(Resource.Error(e))
                }
            } else {
                _storyInformation.postValue(Resource.Error(Exception("No stories found for this user")))
            }
        }.addOnFailureListener {
            _storyInformation.postValue(Resource.Error(it))
        }
    }

    /*
        fun getStories(userId: String, placesId: String) {
            val ref = firestore.collection("Story").document(userId)
            ref.get().addOnSuccessListener { value ->
                if (value != null && value.exists()) {
                    storyList.clear()
                    try {
                        val doc = value.data as HashMap<*, *>
                        val timecurrent = System.currentTimeMillis()
                        for (i in doc) {
                            val story = i.value as HashMap<*, *>
                            val timestart = story["timeStart"] as Long
                            val timeend = story["timeEnd"] as Long
                            val imageurl = story[ConstValues.IMAGE_URL] as String
                            val storyId = story["storyId"] as String
                            val caption = story["caption"] as String
                            val storyPlacesId = story["placesId"] as String

                            if (timecurrent in (timestart + 1) until timeend && storyPlacesId == placesId) {
                                val storyi = Story(
                                    imageUrl = imageurl,
                                    timeStart = timestart,
                                    storyId = storyId,
                                    caption = caption,
                                    placesId = storyPlacesId
                                )
                                storyList.add(storyi)
                            }
                        }
                        if (storyList.isNotEmpty()) {
                            _storyInformation.postValue(Resource.Success(storyList))
                        } else {
                            _storyInformation.postValue(Resource.Error(Exception("No stories available for this place")))
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                        _storyInformation.postValue(Resource.Error(e))
                    }
                } else {
                    _storyInformation.postValue(Resource.Error(Exception("No stories found for this user")))
                }
            }.addOnFailureListener {
                _storyInformation.postValue(Resource.Error(it))
            }
        }
    */

    fun fetchPlaces(placesId: String) {
        firestore.collection("Places").document(placesId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val place = document.toObject(Places::class.java)
                    place?.let {
                        _placesResult.postValue(Resource.Success(it))
                    } ?: run {
                        _placesResult.postValue(Resource.Error(Exception("Place not found")))
                    }
                } else {
                    _placesResult.postValue(Resource.Error(Exception("Place does not exist")))
                }
            }
            .addOnFailureListener { exception ->
                _placesResult.postValue(Resource.Error(exception))
            }
    }
}
