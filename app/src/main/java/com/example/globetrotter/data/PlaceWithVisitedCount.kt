package com.example.globetrotter.data

data class PlaceWithVisitedCount(
    val placeId: String,
    val placeName: String,
    val category: String,
    val visitedCount: Int = 0
)
