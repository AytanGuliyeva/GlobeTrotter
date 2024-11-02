package com.example.globetrotter.ui.placesDetail.peopleVisits.overView

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.R
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.data.Story
import com.example.globetrotter.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OverViewViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
                            val story = Story(
                                imageUrl = imageurl,
                                storyId = storyId,
                                caption = caption,
                                placesId = storyPlacesId
                            )
                            storyList.add(story)
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

    //like
    fun toggleLikeStatus(overviewId: String, imageView: ImageView) {
        val tag = imageView.tag?.toString() ?: ""

        if (tag == "liked") {
            imageView.setImageResource(R.drawable.icon_favourites)
            imageView.tag = "like"
            removeLike(overviewId)
        } else {
            imageView.setImageResource(R.drawable.icon_favourited)
            imageView.tag = "liked"
            addLike(overviewId)
        }
    }

    fun checkLikeStatus(overviewId: String, imageView: ImageView) {
        firestore.collection("LikesOverview").document(overviewId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser = document.getBoolean(auth.currentUser!!.uid) ?: false
                    if (likedByCurrentUser) {
                        imageView.setImageResource(R.drawable.icon_favourited)
                        imageView.tag = "liked"
                    } else {
                        imageView.setImageResource(R.drawable.icon_favourites)
                        imageView.tag = "like"
                    }
                } else {
                    imageView.setImageResource(R.drawable.icon_favourites)
                    imageView.tag = "like"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkFavStatus", "Error checking fav status: $exception")
            }
    }
    private fun addLike(overviewId: String){
        val likeData= hashMapOf(
            auth.currentUser!!.uid to true
        )

        firestore.collection("LikesOverview").document(overviewId)
            .set(likeData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addFavToFirestore", "Fav added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addFavToFirestore", "Error adding fav: $exception")
            }
    }

    private fun removeLike(overviewId: String){
        firestore.collection("LikesOverview").document(overviewId)
            .update(auth.currentUser!!.uid,FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeFavFromFirestore", "Fav removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeFavFromFirestore", "Error removing fav: $exception")
            }
    }
}
