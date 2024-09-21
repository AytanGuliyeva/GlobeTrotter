package com.example.globetrotter.ui.discoverActivities

import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.globetrotter.R
import com.example.globetrotter.base.ConstValues
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.PlaceWithVisitedCount
import com.example.globetrotter.data.Places
import com.example.globetrotter.data.Users
import com.example.globetrotter.retrofit.RepositoryAmadeus
import com.example.globetrotter.retrofit.model.Activity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class DiscoverActivitiesViewModel() : ViewModel() {
    private var firestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()


    private val uniqueCategories = mutableSetOf<String>()


    private val _visitedCount = MutableLiveData<Int>()
    val visitedCount: LiveData<Int>
        get() = _visitedCount

    private val _isVisited = MutableLiveData<Boolean>()
    val isVisited: LiveData<Boolean>
        get() = _isVisited

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    private val _categories = MutableLiveData<Resource<List<String>>>()
    val categories: LiveData<Resource<List<String>>>
        get() = _categories
    private val _places = MutableLiveData<Resource<List<Places>>>()
    val places: LiveData<Resource<List<Places>>> get() = _places


    private val _placesWithVisitedCount = MutableLiveData<List<PlaceWithVisitedCount>>()
    val placesWithVisitedCount: LiveData<List<PlaceWithVisitedCount>>
        get() = _placesWithVisitedCount

    fun fetchPlaces() {
        _places.value = Resource.Loading // Yüklenme durumunu belirt

        // Veriyi Firestore'dan çekme işlemi
        firestore.collection("Places")
            .get()
            .addOnSuccessListener { documents ->
                val placesList = documents.map { document ->
                    document.toObject(Places::class.java) // Place nesnesine dönüştür
                }
                _places.value = Resource.Success(placesList) // Başarılı bir şekilde veriyi al
            }
            .addOnFailureListener { exception ->
                _places.value = Resource.Error(exception) // Hata durumunu belirt
            }
    }
    fun fetchPlacesWithVisitedCount() {
        firestore.collection("Places")
            .get()
            .addOnSuccessListener { placesSnapshot ->
                val placesList = mutableListOf<PlaceWithVisitedCount>()
                val batchSize = placesSnapshot.size()

                for (document in placesSnapshot.documents) {
                    val placeId = document.id
                    val placeName = document.getString("place") ?: ""
                    val category = document.getString("category") ?: ""

                    // Fetch visited count for each place
                    firestore.collection("Visited").document(placeId)
                        .get()
                        .addOnSuccessListener { visitedSnapshot ->
                            val visitedCount = visitedSnapshot.data?.keys?.size ?: 0

                            // Add the place with visited count to the list
                            val placeWithVisitedCount = PlaceWithVisitedCount(
                                placeId = placeId,
                                placeName = placeName,
                                category = category,
                                visitedCount = visitedCount
                            )
                            placesList.add(placeWithVisitedCount)

                            // Once all places are fetched, post the result to LiveData
                            if (placesList.size == batchSize) {
                                _placesWithVisitedCount.postValue(placesList.sortedByDescending { it.visitedCount })
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error fetching visited count: ${exception.localizedMessage}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching places: ${exception.localizedMessage}")
            }
    }


//    fun fetchVisitedCount(placesId: String) {
//        firestore.collection("Visited").document(placesId)
//            .addSnapshotListener { documentSnapshot, error ->
//                if (error != null) {
//                    Log.e(TAG, "Error fetching visited count: ${error.localizedMessage}")
//                    return@addSnapshotListener
//                }
//
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    val visitedUsers = documentSnapshot.data?.keys?.size ?: 0
//                    if (visitedUsers>=3){
//                    _visitedCount.postValue(visitedUsers)}
//                } else {
//                    _visitedCount.postValue(0)
//                }
//            }
//    }

    fun fetchCategories() {
        _categories.postValue(Resource.Loading)
        firestore.collection("Places")
            .get()
            .addOnSuccessListener { documents ->
                val uniqueCategories = mutableSetOf<String>()
                for (document in documents) {
                    document.getString("category")?.trim()?.let { category ->
                        uniqueCategories.add(category)
                    }
                }
                _categories.postValue(Resource.Success(uniqueCategories.toList()))
            }
            .addOnFailureListener { exception ->
                _categories.postValue(Resource.Error(exception))
            }
    }


    fun fetchInformation() {
        _userInformation.postValue(Resource.Loading)
        firestore.collection(ConstValues.USERS)
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toUser()
                    if (user != null) {
                        _userInformation.postValue(Resource.Success(user))
                    } else {
                        _userInformation.postValue(Resource.Error(Exception("User data is null")))
                    }
                } else {
                    _userInformation.postValue(Resource.Error(Exception("User document does not exist")))
                }
            }
            .addOnFailureListener { exception ->
                _userInformation.postValue(Resource.Error(exception))
            }
    }


//    fun fetchCategoriesAndAddChips(chipGroup: ChipGroup) {
//        _categories.postValue(Resource.Loading)
//        firestore.collection("Places")
//            .get()
//            .addOnSuccessListener { documents ->
//                uniqueCategories.clear()
//                chipGroup.removeAllViews()
//                for (document in documents) {
//                    val category = document.getString("category")
//                    if (category != null) {
//                        if (uniqueCategories.add(category.trim())) {
//                            addChipToGroup(chipGroup, category.trim())
//                            Log.d("TAG", "Added new category: $category")
//                        }
//                    }
//                }
//                _categories.postValue(Resource.Success(uniqueCategories.toList()))
//                Log.d("TAG", "Categories fetched successfully: ${uniqueCategories.toList()}")
//            }
//            .addOnFailureListener { exception ->
//                _categories.postValue(Resource.Error(exception))
//            }
//    }
//
//
//    private fun addChipToGroup(chipGroup: ChipGroup, category: String) {
//        val chip = Chip(chipGroup.context)
//        chip.text = category
//        chip.isCheckable = true
//        val chipBorderColor = ContextCompat.getColor(chipGroup.context, R.color.blue)
//        val chipBackgroundColor = ContextCompat.getColor(chipGroup.context, R.color.blue)
//        chip.chipStrokeColor = ColorStateList.valueOf(chipBorderColor)
//
//        val states = arrayOf(
//            intArrayOf(android.R.attr.state_checked),
//            intArrayOf()
//        )
//        val colors = intArrayOf(
//            chipBackgroundColor,
//            Color.TRANSPARENT
//        )
//        val colorStateList = ColorStateList(states, colors)
//
//        chip.chipBackgroundColor = colorStateList
//        chipGroup.addView(chip)
//    }
fun fetchCategoriesAndAddChips(chipGroup: ChipGroup) {
    _categories.postValue(Resource.Loading)
    firestore.collection("Places")
        .get()
        .addOnSuccessListener { documents ->
            uniqueCategories.clear()
            chipGroup.removeAllViews()
            for (document in documents) {
                document.getString("category")?.trim()?.let { category ->
                    if (uniqueCategories.add(category)) {
                        addChipToGroup(chipGroup, category)
                    }
                }
            }
            _categories.postValue(Resource.Success(uniqueCategories.toList()))
        }
        .addOnFailureListener { exception ->
            _categories.postValue(Resource.Error(exception))
        }
}

    private fun addChipToGroup(chipGroup: ChipGroup, category: String) {
        val context = chipGroup.context
        val chip = Chip(context).apply {
            isCheckable = true
            text = category
            setChipIconByCategory(context, this, category)
            setupChipStyle(context, this)
        }
//        chip.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                selectedCategory = category
//                selectedCategories.add(category)
//                fetchPlacesByCategories()
//            } else {
//                selectedCategories.remove(category)
//            }
//        }
        chipGroup.addView(chip)
    }

    private fun setChipIconByCategory(context: Context, chip: Chip, category: String) {
        val iconResId = when (category) {
            "Historical" -> R.drawable.icon_historical
            "Natural Wonders" -> R.drawable.icon_natural_wonders
            "Mountains" -> R.drawable.icon_mountain
            "Beaches" -> R.drawable.icon_beach
            "Camping & Hiking" -> R.drawable.icon_camping
            "Urban Exploration" -> R.drawable.icon_urban
            "Islands" -> R.drawable.icon_island
            "Cultural Experiences" -> R.drawable.icon_cultural
            else -> 0
        }
        if (iconResId != 0) {
            chip.chipIcon = ContextCompat.getDrawable(context, iconResId)
            chip.chipIconTint = ColorStateList.valueOf(Color.BLUE)
            chip.chipIconSize = 48f
        }
    }

    private fun setupChipStyle(context: Context, chip: Chip) {
        val chipParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        chip.layoutParams = chipParams
        chip.chipStartPadding = 16f
        chip.iconStartPadding = 8f
        chip.iconEndPadding = 8f
        chip.chipEndPadding = 16f
        chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue))
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        )
        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.blue),
            Color.TRANSPARENT
        )
        chip.chipBackgroundColor = ColorStateList(states, colors)
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