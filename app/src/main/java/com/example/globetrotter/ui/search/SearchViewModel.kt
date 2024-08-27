package com.example.globetrotter.ui.search

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.globetrotter.R
import com.example.globetrotter.base.Resource
import com.example.globetrotter.data.Places
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private var firestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val uniqueCategories = mutableSetOf<String>()

    private val _categories = MutableLiveData<Resource<List<String>>>()
    val categories: LiveData<Resource<List<String>>>
        get() = _categories

    private val _placesResult = MutableLiveData<Resource<List<Places>>>()
    val placesResult:LiveData<Resource<List<Places>>>
        get() = _placesResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun fetchCategoriesAndAddChips(chipGroup: ChipGroup) {

        _categories.postValue(Resource.Loading)
        firestore.collection("Places")
            .get()
            .addOnSuccessListener { documents->
                uniqueCategories.clear()
                chipGroup.removeAllViews()
                for (document in documents){
                    val category = document.getString("category")
                    if (category != null){
                        if (uniqueCategories.add(category.trim())){
                            addChipToGroup(chipGroup,category.trim())
                            Log.d("TAG", "Added new category: $category")
                        }
                    }
                }
                _categories.postValue(Resource.Success(uniqueCategories.toList()))
                Log.d("TAG", "Categories fetched successfully: ${uniqueCategories.toList()}")
            }
            .addOnFailureListener { exception ->
                _categories.postValue(Resource.Error(exception))
            }
    }

    private fun addChipToGroup(chipGroup: ChipGroup, category: String) {
        val context = chipGroup.context
        val chipContainer = LinearLayout(context)
        chipContainer.orientation = LinearLayout.VERTICAL
        val containerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        containerParams.setMargins(10, 8, 8, 8)
        chipContainer.layoutParams = containerParams

        val chip = Chip(context)
        chip.isCheckable = true

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

        chip.text = ""

        val chipParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        chip.layoutParams = chipParams

        val chipBorderColor = ContextCompat.getColor(context, R.color.blue)
        val chipBackgroundColor = ContextCompat.getColor(context, R.color.blue)
        chip.chipStrokeColor = ColorStateList.valueOf(chipBorderColor)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        )
        val colors = intArrayOf(
            chipBackgroundColor,
            Color.TRANSPARENT
        )
        val colorStateList = ColorStateList(states, colors)
        chip.chipBackgroundColor = colorStateList

        chipContainer.addView(chip)

        val textView = TextView(context)
        textView.text = category
        textView.setTextColor(Color.BLACK)
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER_HORIZONTAL

        textView.maxWidth = 120
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.isSingleLine = true

        val textParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.layoutParams = textParams

        chipContainer.addView(textView)

        chipGroup.addView(chipContainer)
    }


    fun fetchPlaces() {
        _loading.postValue(true)
        firestore.collection("Places").get()
            .addOnSuccessListener { documents ->
                val placesList = mutableListOf<Places>()
                for (placeDocs in documents) {
                    val category = placeDocs.getString("category") ?: ""
                    val description = placeDocs.getString("description") ?: ""
                    val location = placeDocs.getString("location") ?: ""
                    val place = placeDocs.getString("place") ?: ""
                    val placeImageUrls = placeDocs.get("placeImageUrls") as? List<String> ?: emptyList()
                    val placesId = placeDocs.getString("placesId") ?: ""
                    val price = placeDocs.getString("price") ?: ""

                    val places = Places(
                        category = category,
                        description = description,
                        location = location,
                        place = place,
                        placeImageUrls = placeImageUrls,
                        placesId = placesId,
                        price = price
                    )

                    placesList.add(places)
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