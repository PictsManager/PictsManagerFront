package com.pictsmanager.request.model

class ImageModel(var id: Long, var name: String, var image: ByteArray, var owner_id: Long, var access_read: Boolean) {

    override fun toString(): String {
        return "Id:$id\nName:$name\n" +
                "Owner_id:$owner_id\n" +
                "Access_read:$access_read"
    }
}