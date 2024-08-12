package com.example.globetrotter.ui.discoverActivities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.globetrotter.retrofit.RepositoryAmadeus
import com.example.globetrotter.retrofit.model.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class DiscoverActivitiesViewModel() : ViewModel() {
    private val repository = RepositoryAmadeus()

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> get() = _activities

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

     fun fetchActivities(latitude: Double, longitude: Double, radius: Int) {
            viewModelScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        repository.getActivities(latitude, longitude, radius)
                    }
                    _activities.postValue(response.data)
                    Log.d("TAG", "fetchActivities: ${response.data}")
                } catch (e: Exception) {
                    _error.postValue("Failed to fetch activities: ${e.message}")
                    Log.e("TAG", "Error: ${e.message}")
                }
            }
        }
}