package com.example.globetrotter.ui.favourites

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.data.PlacesInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _placesResult = MutableLiveData<Resource<List<Places>>>()
    val placesResult: LiveData<Resource<List<Places>>> get() = _placesResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _categoryDrawable = MutableLiveData<Int>()
    val categoryDrawable: LiveData<Int>
        get() = _categoryDrawable

    init {
        fetchFavPlacesWithListener()
    }

    fun fetchFavPlacesWithListener() {
        _loading.postValue(true)

        firestore.collection("Likes").document(auth.currentUser!!.uid)
            .addSnapshotListener { document, exception ->
                if (exception != null) {
                    Log.e("fetchFavPlaces", "Listen failed: $exception")
                    _placesResult.postValue(Resource.Error(exception))
                    _loading.postValue(false)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val favPlacesIds = document.data?.keys?.toList() ?: emptyList()

                    if (favPlacesIds.isNotEmpty()) {
                        firestore.collection("Places")
                            .whereIn(FieldPath.documentId(), favPlacesIds)
                            .addSnapshotListener { querySnapshot, exception ->
                                if (exception != null) {
                                    Log.e("fetchFavPlaces", "Error fetching fav places: $exception")

                                    _placesResult.postValue(Resource.Error(exception))
                                    return@addSnapshotListener
                                }

                                val placesList =
                                    querySnapshot?.toObjects(Places::class.java) ?: emptyList()
                                _placesResult.postValue(Resource.Success(placesList))
                            }
                    } else {
                        _placesResult.postValue(Resource.Success(emptyList()))
                    }
                } else {
                    _placesResult.postValue(Resource.Success(emptyList()))
                }
                _loading.postValue(false)
            }
    }

}
