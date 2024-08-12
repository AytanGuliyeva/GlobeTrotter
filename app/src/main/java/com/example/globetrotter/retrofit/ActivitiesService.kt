package com.example.globetrotter.retrofit

import com.example.globetrotter.retrofit.model.ActivitiesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ActivitiesService {
    @GET("v1/shopping/activities")
    suspend fun getActivities(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int
    ): ActivitiesResponse
}