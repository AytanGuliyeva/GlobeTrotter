package com.example.globetrotter.data


data class Activity(
    val type: String,
    val id: String,
    val self: Self,
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val geoCode: GeoCode,
    val price: Price? = null,
    val pictures: List<String>,
    val minimumDuration: String? = null
)

data class Self(
    val href: String,
    val methods: List<String>
)

data class GeoCode(
    val latitude: Double,
    val longitude: Double
)

data class Price(
    val currencyCode: String? = null
)