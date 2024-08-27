package com.example.globetrotter.data

data class Places(
    val category: String="",
    val description: String="",
    val location: String="",
    val place: String="",
    val placeImageUrls: List<String>,
    val placesId: String="",
    val price: String="",
    var visited:Boolean=false,
    var isLiked:Boolean=false
)
