package com.pictsmanager.request.api

import com.pictsmanager.request.model.ImageModel
import retrofit2.Call
import retrofit2.http.*

interface ImageApi {

    @POST("/image/create")
    fun createImage(@Field("name") name: String, @Field("access_read") access_read: Boolean, @Field("image") image: ArrayList<Int>): Call<Any>

    @DELETE("/image/delete")
    fun deleteImage(@Query("id") id: Long): Call<Any>

    @DELETE("/image/delete/multiple")
    fun deleteImages(@Field("ids") ids: ArrayList<Long>): Call<Any>

    @PUT("/image/update")
    fun updateImage(@Field("id") id: Long, @Field("name") name: String, @Field("acces_read") access_read: Boolean): Call<Any>

    @GET("/image/read")
    fun readImages(@Query("id") id: Long?): Call<ArrayList<ImageModel>>

    @GET("/image/public")
    fun readPublic(): Call<ArrayList<ImageModel>>

}