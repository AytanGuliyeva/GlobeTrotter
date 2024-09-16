package com.example.globetrotter.data

data class PlacesInfo(
    var user:Users?=null,
    var places: Places,
    var commentCount:Int=0,
    var visitedCount:Int=0
)