package com.example.globetrotter.ui.search

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.example.globetrotter.ui.search.adapter.PlacesAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var placesList: List<Places> = emptyList()
    private var isPlacesFetched = false
    private var selectedCategory: String? = null
    private val selectedCategories = mutableListOf<String>()

    private val uniqueCategories = mutableSetOf<String>()

    private val _categories = MutableLiveData<Resource<List<String>>>()
    val categories: LiveData<Resource<List<String>>> get() = _categories

    private val _placesResult = MutableLiveData<Resource<List<Places>>>()
    val placesResult: LiveData<Resource<List<Places>>> get() = _placesResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

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
        val trimmedCategory = category.trim()
        val chip = Chip(context).apply {
            isCheckable = true
            text = trimmedCategory
            setChipIconByCategory(context, this, trimmedCategory)
            //   text = category
            // setChipIconByCategory(context, this, category)
            setupChipStyle(context, this)
        }
        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedCategory = category
                selectedCategories.add(category)
                fetchPlacesByCategories()
            } else {
                selectedCategories.remove(category)
            }
            fetchPlacesByCategories()
        }

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

    fun fetchPlaces() {
        _loading.postValue(true)

        firestore.collection("Places").get()
            .addOnSuccessListener { documents ->
                placesList = documents.mapNotNull { document ->
                    document.toObject(Places::class.java)
                }
                _placesResult.postValue(Resource.Success(placesList))
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                _placesResult.postValue(Resource.Error(exception))
                _loading.postValue(false)
            }
    }

    fun fetchPlacesByCategories() {

        if (selectedCategories.isEmpty()) {
            Log.d("SearchViewModel", "No categories selected")
            fetchPlaces()
            return
        }

        Log.d("SearchViewModel", "Selected categories: $selectedCategories")

        _loading.postValue(true)
        firestore.collection("Places")
            .whereIn("category", selectedCategories.map { it.trim() })
            .get()
            .addOnSuccessListener { documents ->
                val filteredPlaces = documents.mapNotNull { it.toObject(Places::class.java) }
                _placesResult.postValue(Resource.Success(filteredPlaces))
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                _placesResult.postValue(Resource.Error(exception))
                _loading.postValue(false)
            }
    }

    fun searchPlaces(query: String) {
        _loading.postValue(true)
        firestore.collection("Places")
            .orderBy("location")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val placesList = documents.mapNotNull { document ->
                    document.toObject(Places::class.java)
                }
                _placesResult.postValue(Resource.Success(placesList))
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                _placesResult.postValue(Resource.Error(exception))
                _loading.postValue(false)
            }
    }
}
