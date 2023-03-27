package com.sid.notificationdownloadprogressbar

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitHelper {
//    val baseUrl = "https://www.google.com/"
//    val baseUrl = "https://images.pexels.com/"
    val baseUrl = "https://sample-videos.com/"

    var gson = GsonBuilder()
        .setLenient()
        .create()

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}