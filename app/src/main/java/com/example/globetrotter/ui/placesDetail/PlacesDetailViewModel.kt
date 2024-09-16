package com.example.globetrotter.ui.placesDetail

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.ImageView
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

class PlacesDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var category: String = ""

    private val _placesResult = MutableLiveData<Resource<Places>>()
    val placesResult: LiveData<Resource<Places>>
        get() = _placesResult

    private val _categoryDrawable = MutableLiveData<Int>()
    val categoryDrawable: LiveData<Int>
        get() = _categoryDrawable

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    //fetch places

    fun fetchPlaces(placesId: String) {
        _loading.postValue(true)
        val placesDocumentRef = firestore.collection("Places").document(placesId)
        Log.d("TAG", "fetchPlaces: $placesId")
        placesDocumentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                Log.d("TAG", "fetchPlaces: success")
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

    //like
    fun toggleLikeStatus(placesId: String,imageView:ImageView){
        val  tag = imageView.tag?.toString() ?: ""

        if (tag == "favourited"){
            imageView.setImageResource(R.drawable.icon_favourites)
            imageView.tag = "favourite"
            removeFavFromFirestore(placesId)
        }else{
            imageView.setImageResource(R.drawable.icon_favourited)
            imageView.tag = "favourited"
           addFavToFirestore(placesId)
        }
    }
    fun checkFavStatus(placesId: String,imageView:ImageView){

        firestore.collection("Likes").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    val likedByCurrentUser= document.getBoolean(placesId) ?: false
                    if (likedByCurrentUser){
                        imageView.setImageResource(R.drawable.icon_favourited)
                            imageView.tag = "favourited"
                    }else{
                        imageView.setImageResource(R.drawable.icon_favourites)
                        imageView.tag = "favourite" 
                    }
                }else{
                    imageView.setImageResource(R.drawable.icon_favourites)
                    imageView.tag="favourite"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkFavStatus", "Error checking fav status: $exception", )
            }
    }
    private fun addFavToFirestore(placesId: String){
        val favData = hashMapOf(
            placesId to true
        )

        firestore.collection("Likes").document(auth.currentUser!!.uid).set(favData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addFavToFirestore", "Fav added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addFavToFirestore", "Error adding fav: $exception")
            }
    }
    private fun removeFavFromFirestore(placesId: String){
        firestore.collection("Likes").document(auth.currentUser!!.uid)
            .update(placesId, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeFavFromFirestore", "Fav removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeFavFromFirestore", "Error removing fav: $exception")
            }
    }

    //visit
    fun toggleVisitedStatus(placesId: String,imageView:ImageView){
        val  tag = imageView.tag?.toString() ?: ""

        if (tag == "visited"){
            imageView.setImageResource(R.drawable.icon_visited)
            imageView.tag = "visit"
            removeVisitedFromFirestore(placesId)
        }else{
            imageView.setImageResource(R.drawable.icon_visited2)
            imageView.tag = "visited"
            addVisitToFirestore(placesId)
        }
    }
    fun checkVisitStatus(placesId: String,imageView:ImageView){

        firestore.collection("Visited").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    val likedByCurrentUser= document.getBoolean(placesId) ?: false
                    if (likedByCurrentUser){
                        imageView.setImageResource(R.drawable.icon_visited2)
                        imageView.tag = "visited"
                    }else{
                        imageView.setImageResource(R.drawable.icon_visited)
                        imageView.tag = "visit"
                    }
                }else{
                    imageView.setImageResource(R.drawable.icon_visited)
                    imageView.tag="visit"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkVisitStatus", "Error checking visit status: $exception", )
            }
    }
    private fun addVisitToFirestore(placesId: String){
        val favData = hashMapOf(
            placesId to true
        )

        firestore.collection("Visited").document(auth.currentUser!!.uid).set(favData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addVisitedToFirestore", "Visited added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addVisitedToFirestore", "Error adding Visited: $exception")
            }
    }
    private fun removeVisitedFromFirestore(placesId: String){
        firestore.collection("Visited").document(auth.currentUser!!.uid)
            .update(placesId, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeFavFromFirestore", "Fav removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeFavFromFirestore", "Error removing fav: $exception")
            }
    }

}
