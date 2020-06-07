package com.pictsmanager.request.api

import com.pictsmanager.request.model.AlbumModel
import retrofit2.Call
import retrofit2.http.*

interface AlbumApi {

    @POST("/album/create")
    fun createAlbum(@Field("name") name: String, @Field("access_read") access_read: Boolean, @Field("images") album: ArrayList<Int>): Call<Any>

    @DELETE("/album/delete")
    fun deleteAlbum(@Query("id") id: Long): Call<Any>

    @DELETE("/album/delete/multiple")
    fun deleteAlbums(@Field("ids") ids: ArrayList<Long>): Call<Any>

    @PUT("/album/update")
    fun updateAlbum(@Field("id") id: Long, @Field("name") name: String, @Field("access_read") access_read: Boolean, @Field("images") images: ArrayList<Long>): Call<Any>

    @GET("/album/read")
    fun readAlbums(@Query("id") id: Long?): Call<ArrayList<AlbumModel>>

    @GET("/album/public")
    fun readPublic(): Call<ArrayList<AlbumModel>>

}