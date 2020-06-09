package com.pictsmanager.request.api

import com.pictsmanager.request.model.UserModel
import retrofit2.Call
import retrofit2.http.*


interface UsersApi {

    @GET("/user/login")
    fun connexion(@Query("email") email: String, @Query("password") password: String): Call<Any>

    @POST("/user/register")
    fun createAccount(@Field("email") email: String, @Field("password") password: String): Call<Any>

}