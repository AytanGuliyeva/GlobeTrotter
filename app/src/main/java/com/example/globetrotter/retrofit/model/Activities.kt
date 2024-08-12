package com.example.globetrotter.retrofit.model

data class ActivitiesResponse(
    val data: List<Activity>
)

data class Activity(
    val name: String,
    val description: String
)