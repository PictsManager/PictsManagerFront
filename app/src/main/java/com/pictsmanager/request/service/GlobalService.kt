package com.pictsmanager.request.service

import com.pictsmanager.request.api.UsersApi
import com.pictsmanager.util.GlobalStatus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder

import com.google.gson.Gson
import com.pictsmanager.request.api.AlbumApi
import com.pictsmanager.request.api.ImageApi


class GlobalService {

    companion object {
        private var okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        private var gson: Gson? = GsonBuilder()
            .setLenient()
            .create()

        var userService: UsersApi = Retrofit.Builder()
            .baseUrl(GlobalStatus.API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(UsersApi::class.java)

        var imageService: ImageApi = Retrofit.Builder()
            .baseUrl(GlobalStatus.API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(ImageApi::class.java)

        var albumService: AlbumApi = Retrofit.Builder()
            .baseUrl(GlobalStatus.API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(AlbumApi::class.java)
    }
}