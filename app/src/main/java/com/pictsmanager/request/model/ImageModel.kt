package com.pictsmanager.request.model

class ImageModel(var id: Long, var name: String, var image: ByteArray, var owner_id: Long, var access_read: Boolean) {}