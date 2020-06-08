package com.pictsmanager.request.api

import com.pictsmanager.request.model.AlbumModel
import retrofit2.Call
import retrofit2.http.*

interface AlbumApi {

    @FormUrlEncoded
    @POST("/album/create")
    fun createAlbum(@Header("authorization") authorization: String, @Field("name") name: String, @Field("access_read") access_read: Boolean): Call<Any>

    @DELETE("/album/delete")
    fun deleteAlbum(@Header("authorization") authorization: String, @Query("id") id: Long): Call<Any>

    @FormUrlEncoded
    @POST("/album/delete/multiple")
    fun deleteAlbums(@Header("authorization") authorization: String, @Field("ids") ids: ArrayList<Long>): Call<Any>

    @FormUrlEncoded
    @PUT("/album/update")
    fun updateAlbum(@Header("authorization") authorization: String, @Field("id") id: Long, @Field("name") name: String, @Field("access_read") access_read: Boolean, @Field("images") images: ArrayList<Long>?): Call<Any>

    @GET("/album/read")
    fun readAlbums(@Header("authorization") authorization: String, @Query("id") id: Long?): Call<ArrayList<AlbumModel>>

    @GET("/album/public")
    fun readPublic(@Header("authorization") authorization: String): Call<ArrayList<AlbumModel>>

}