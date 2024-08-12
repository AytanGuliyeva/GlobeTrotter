package com.example.globetrotter.retrofit

import com.example.globetrotter.retrofit.model.ActivitiesResponse

class RepositoryAmadeus() {
    val activitiesService=RetrofitInstance.getAmadeusInstance().create(ActivitiesService::class.java)

    suspend fun getActivities(
        latitude: Double,
        longitude: Double,
        radius: Int
    ): ActivitiesResponse {
        return activitiesService.getActivities(latitude, longitude, radius)
    }

}
