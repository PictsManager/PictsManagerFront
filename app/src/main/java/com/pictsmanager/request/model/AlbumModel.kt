package com.pictsmanager.request.model

class AlbumModel(var id: Long, var name: String, var images: ArrayList<Long>, var owner_id: Long, var access_read: Boolean) {

    override fun toString(): String {
        return "Id:$id\nName:$name\n" +
                "Owner_id:$owner_id\n" +
                "Access_read:$access_read"
    }
}