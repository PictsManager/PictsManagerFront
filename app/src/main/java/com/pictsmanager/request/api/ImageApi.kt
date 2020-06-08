package com.pictsmanager.request.api

import android.graphics.Bitmap
import com.pictsmanager.request.model.ImageModel
import com.pictsmanager.request.model.SuccessModel
import retrofit2.Call
import retrofit2.http.*
import java.io.FileOutputStream


interface ImageApi {

    @POST("/image/create")
    fun tryAddImage(@Header("authorization") authorization: String, @Body imageModel: ImageModel): Call<SuccessModel>

/*
    @POST("/image/delete")
    fun tryCreateAccount(@Body userModel: UserModel): Call<UserModel>
*/

/*    @POST("/api/users")
    fun createUser(@Body user: UserModel): Call<UserModel>*/

}