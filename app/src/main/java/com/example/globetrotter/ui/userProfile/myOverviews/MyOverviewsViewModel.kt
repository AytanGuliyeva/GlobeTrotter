package com.example.globetrotter.ui.userProfile.myOverviews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Story
import com.example.globetrotter.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MyOverviewsViewModel : ViewModel() {
    private var firestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val overviewList = ArrayList<Story>()

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    private val _storyInformation = MutableLiveData<Resource<Map<String, Story>>>()
    val storyInformation: LiveData<Resource<Map<String, Story>>>
        get() = _storyInformation


    private val _overviewDeleted = MutableLiveData<Resource<Boolean>>()
    val overviewDeleted: LiveData<Resource<Boolean>>
        get() = _overviewDeleted


    /*
        fun getOverviews() {
            val ref = firestore.collection("Story").document(auth.currentUser!!.uid)
            ref.get().addOnSuccessListener { value ->
                if (value != null && value.exists()) {
                    overviewList.clear()
                    try {
                        val doc = value.data as HashMap<*, *>
                        for (i in doc) {
                            val story = i.value as HashMap<*, *>
                            val imageurl = story[ConstValues.IMAGE_URL] as String
                            val storyId = story["storyId"] as String
                            val caption = story["caption"] as String
                            val storyPlacesId = story["placesId"] as String

                            val storyi = Story(
                                imageUrl = imageurl,
                                storyId = storyId,
                                caption = caption,
                                placesId = storyPlacesId
                            )
                            overviewList.add(storyi)
                            _storyInformation.postValue(Resource.Success(overviewList))


                        }
                    } catch (e: java.lang.NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    */


    fun getOverviews() {
        val ref = firestore.collection("Story").document(auth.currentUser!!.uid)
        ref.get().addOnSuccessListener { value ->
            if (value != null && value.exists()) {
                overviewList.clear()
                val storyMap = mutableMapOf<String, Story>()
                val doc = value.data as HashMap<*, *>

                val placesIds = doc.values.map {
                    (it as HashMap<*, *>)["placesId"] as String
                }.toSet()

                if (placesIds.isNotEmpty()){
                firestore.collection("Places").whereIn("placesId", placesIds.toList()).get()
                    .addOnSuccessListener { placeDocs ->
                        val placeNameMap = placeDocs.documents.associateBy(
                            { it.id },
                            { it.getString("place") ?: "Unknown" }
                        )

                        for (i in doc) {
                            val story = i.value as HashMap<*, *>
                            val imageurl = story[ConstValues.IMAGE_URL] as String
                            val storyId = story["storyId"] as String
                            val caption = story["caption"] as String
                            val storyPlacesId = story["placesId"] as String

                            val storyItem = Story(
                                imageUrl = imageurl,
                                storyId = storyId,
                                caption = caption,
                                placesId = storyPlacesId
                            )

                            val placeName = placeNameMap[storyPlacesId] ?: "Unknown"
                            storyMap[placeName] = storyItem
                        }

                        _storyInformation.postValue(Resource.Success(storyMap))
                    }
            }}
        }
    }


    fun deleteOverview(placesId: String) {
        firestore.collection("Story").document(auth.currentUser!!.uid).update(mapOf(placesId to FieldValue.delete()))
            .addOnSuccessListener {
                _overviewDeleted.postValue(Resource.Success(true))
                getOverviews()
            }
            .addOnFailureListener {
            }
    }

}