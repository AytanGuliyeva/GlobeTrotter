package com.example.globetrotter.data

import java.security.Timestamp

data class Overviews(
    val overview: String,
    val userId: String,
    val placesId: String,
    val overviewId: String,
    val time: Timestamp
)