package com.pictsmanager.request.api

import com.pictsmanager.request.model.ImageModel
import retrofit2.Call
import retrofit2.http.*

interface ImageApi {

    @POST("/image/create")
    fun createImage(@Header("authorization") authorization: String, @Body imageModel: ImageModel): Call<Any>

    @DELETE("/image/delete")
    fun deleteImage(@Header("authorization") authorization: String, @Query("id") id: Long): Call<Any>

    @FormUrlEncoded
    @POST("/image/delete/multiple")
    fun deleteImages(@Header("authorization") authorization: String, @Field("ids") ids: ArrayList<Long>): Call<Any>

    @FormUrlEncoded
    @PUT("/image/update")
    fun updateImage(@Header("authorization") authorization: String, @Field("id") id: Long, @Field("name") name: String, @Field("access_read") access_read: Boolean): Call<Any>

    @GET("/image/read")
    fun readImages(@Header("authorization") authorization: String, @Query("id") id: Long?): Call<ArrayList<ImageModel>>

    @GET("/image/public")
    fun readPublic(@Header("authorization") authorization: String): Call<ArrayList<ImageModel>>
}