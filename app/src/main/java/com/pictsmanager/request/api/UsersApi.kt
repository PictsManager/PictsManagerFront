package com.pictsmanager.request.api

import com.pictsmanager.request.model.SuccessModel
import com.pictsmanager.request.model.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface UsersApi {

    @GET("/user/login")
    fun tryConnexion(@Query("email") email: String, @Query("password") password: String): Call<SuccessModel>

    @POST("/user/register")
    fun tryCreateAccount(@Body userModel: UserModel): Call<UserModel>

/*    @POST("/api/users")
    fun createUser(@Body user: UserModel): Call<UserModel>*/

}