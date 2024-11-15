package com.example.globetrotter.ui.placesDetail

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlacesDetailViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var category: String = ""

    private val _placesResult = MutableLiveData<Resource<Places>>()
    val placesResult: LiveData<Resource<Places>>
        get() = _placesResult

    private val _categoryDrawable = MutableLiveData<Int>()
    val categoryDrawable: LiveData<Int>
        get() = _categoryDrawable

    private val _visitedCount = MutableLiveData<Int>()
    val visitedCount: LiveData<Int>
        get() = _visitedCount

    private val _isVisited = MutableLiveData<Boolean>()
    val isVisited: LiveData<Boolean>
        get() = _isVisited

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _visitedUserProfileImages = MutableLiveData<List<String>>()
    val visitedUserProfileImages: LiveData<List<String>>
        get() = _visitedUserProfileImages


    // Fetch places
    fun fetchPlaces(placesId: String) {
        _loading.postValue(true)
        val placesDocumentRef = firestore.collection("Places").document(placesId)
        Log.d(TAG, "fetchPlaces: $placesId")
        placesDocumentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                Log.d(TAG, "fetchPlaces: success")
                val places = documentSnapshot.toObject(Places::class.java)
                if (places != null) {
                    category = places.category.trim()

                    val drawableRes = when (category) {
                        "Historical" -> R.drawable.illus_historical
                        "Natural Wonders" -> R.drawable.illus_nature
                        "Mountains" -> R.drawable.illus_mountain
                        "Beaches" -> R.drawable.illus_beach
                        "Camping & Hiking" -> R.drawable.illus_camping
                        "Urban Exploration" -> R.drawable.illus_urban
                        "Islands" -> R.drawable.illus_island
                        "Cultural Experiences" -> R.drawable.illus_cultural
                        else -> R.drawable.travel_illustration
                    }

                    _categoryDrawable.postValue(drawableRes)
                    _placesResult.postValue(Resource.Success(places))
                } else {
                    _placesResult.postValue(Resource.Error(Exception("Places data is null")))
                }
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Places fetch failure: ${exception.message}")
                _placesResult.postValue(Resource.Error(exception))
                _loading.postValue(false)
            }
    }

    // Fetch visited count
    fun fetchVisitedCount(placesId: String) {
        firestore.collection("Visited").document(placesId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching visited count: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val visitedUsers = documentSnapshot.data?.keys?.size ?: 0
                    _visitedCount.postValue(visitedUsers)
                } else {
                    _visitedCount.postValue(0)
                }
            }
    }

    //fetch people visits
    fun fetchPeopleVisits(placesId: String) {
        firestore.collection("Visited").document(placesId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching visited users: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val users = documentSnapshot.data?.keys ?: emptySet()
                    val profileImageUrls = mutableListOf<String>()

                    for (userId in users) {
                        firestore.collection("Users").document(userId)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument.exists()) {
                                    val profileImageUrl = userDocument.getString("imageUrl")
                                    val imageUrl = profileImageUrl ?: "default_profile_image_url"
                                    profileImageUrls.add(imageUrl)
                                    if (profileImageUrls.size == users.size) {
                                        _visitedUserProfileImages.postValue(profileImageUrls)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error fetching user profile: ${e.localizedMessage}")
                            }
                    }
                }
            }
    }



    // Visited click listener
    fun visitedClickListener(placesId: String) {
        val currentUserUid = auth.currentUser!!.uid

        if (_isVisited.value == true) {
            firestore.collection("Visited").document(placesId)
                .update(currentUserUid, FieldValue.delete())
                .addOnSuccessListener {
                    _isVisited.postValue(false)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error removing visit: ${exception.localizedMessage}")
                }
        } else {
            val visitData = hashMapOf(currentUserUid to true)
            firestore.collection("Visited").document(placesId)
                .set(visitData, SetOptions.merge())
                .addOnSuccessListener {
                    _isVisited.postValue(true)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error adding visit: ${exception.localizedMessage}")
                }
        }
    }


    // Like
    fun toggleLikeStatus(placesId: String, imageView: ImageView) {
        val tag = imageView.tag?.toString() ?: ""

        if (tag == "favourited") {
            imageView.setImageResource(R.drawable.icon_favourites)
            imageView.tag = "favourite"
            removeFavFromFirestore(placesId)
        } else {
            imageView.setImageResource(R.drawable.icon_favourited)
            imageView.tag = "favourited"
            addFavToFirestore(placesId)
        }
    }

    fun checkFavStatus(placesId: String, imageView: ImageView) {
        firestore.collection("Likes").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser = document.getBoolean(placesId) ?: false
                    if (likedByCurrentUser) {
                        imageView.setImageResource(R.drawable.icon_favourited)
                        imageView.tag = "favourited"
                    } else {
                        imageView.setImageResource(R.drawable.icon_favourites)
                        imageView.tag = "favourite"
                    }
                } else {
                    imageView.setImageResource(R.drawable.icon_favourites)
                    imageView.tag = "favourite"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkFavStatus", "Error checking fav status: $exception")
            }
    }

    private fun addFavToFirestore(placesId: String) {
        val favData = hashMapOf(
            placesId to true
        )

        firestore.collection("Likes").document(auth.currentUser!!.uid)
            .set(favData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addFavToFirestore", "Fav added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addFavToFirestore", "Error adding fav: $exception")
            }
    }

    private fun removeFavFromFirestore(placesId: String) {
        firestore.collection("Likes").document(auth.currentUser!!.uid)
            .update(placesId, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeFavFromFirestore", "Fav removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeFavFromFirestore", "Error removing fav: $exception")
            }
    }

    // Visit
    fun toggleVisitedStatus(placesId: String, imageView: ImageView) {
        val tag = imageView.tag?.toString() ?: ""

        if (tag == "visited") {
            imageView.setImageResource(R.drawable.icon_visited)
            imageView.tag = "visit"
            removeVisitedFromFirestore(placesId)
        } else {
            imageView.setImageResource(R.drawable.icon_visited2)
            imageView.tag = "visited"
            addVisitToFirestore(placesId)
        }
    }

    fun checkVisitStatus(placesId: String, imageView: ImageView) {
        firestore.collection("Visited").document(placesId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val visitedByCurrentUser = document.getBoolean(auth.currentUser!!.uid) ?: false
                    if (visitedByCurrentUser) {
                        imageView.setImageResource(R.drawable.icon_visited2)
                        imageView.tag = "visited"
                    } else {
                        imageView.setImageResource(R.drawable.icon_visited)
                        imageView.tag = "visit"
                    }
                } else {
                    imageView.setImageResource(R.drawable.icon_visited)
                    imageView.tag = "visit"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkVisitStatus", "Error checking visit status: $exception")
            }
    }

    private fun addVisitToFirestore(placesId: String) {
        val visitData = hashMapOf(
            auth.currentUser!!.uid to true
        )

        firestore.collection("Visited").document(placesId)
            .set(visitData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addVisitToFirestore", "Visited added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addVisitToFirestore", "Error adding visited: $exception")
            }
    }

    private fun removeVisitedFromFirestore(placesId: String) {
        firestore.collection("Visited").document(placesId)
            .update(auth.currentUser!!.uid, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeVisitedFromFirestore", "Visited removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeVisitedFromFirestore", "Error removing visited: $exception")
            }
    }
}
