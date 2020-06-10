package com.pictsmanager.request.model

import java.util.*

class ImageModel(var id: Long, var owner_id: Long, var url: String, var name: String, var date_creation: String, var access_read: Boolean, var image: ByteArray) {

    override fun toString(): String {
        return "Id:$id\nName:$name\n" +
                "Owner_id:$owner_id\n" +
                "Access_read:$access_read"
    }
}