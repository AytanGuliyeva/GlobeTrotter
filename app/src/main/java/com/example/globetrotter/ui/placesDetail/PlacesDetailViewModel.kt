package com.example.globetrotter.ui.placesDetail

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlacesDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var category:String=""

    private val _placesResult = MutableLiveData<Resource<Places>>()
    val placesResult: LiveData<Resource<Places>>
        get() = _placesResult

    private val _categoryDrawable = MutableLiveData<Int>()
    val categoryDrawable: LiveData<Int>
        get() = _categoryDrawable

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

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
    }}
