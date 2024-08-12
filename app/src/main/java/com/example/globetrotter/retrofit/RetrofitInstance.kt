package com.example.globetrotter.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val AMADEUS_BASE_URL = "https://test.api.amadeus.com/"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val TOKEN = "CmOUkmydOWyfdawFiGhbfqfNcafuh0wd"

    private val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithAuth = originalRequest.newBuilder()
            .header("Authorization", "Bearer $TOKEN")
            .build()
        chain.proceed(requestWithAuth)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    // Amadeus Retrofit Instance
    fun getAmadeusInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AMADEUS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    // Nominatim Retrofit Instance
    fun getNominatimInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}
