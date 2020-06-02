package com.pictsmanager.request.model

class AlbumModel(var id: Long, var name: String, var images: ArrayList<ImageModel>, var owner_id: Long, var access_read: Boolean) {
}