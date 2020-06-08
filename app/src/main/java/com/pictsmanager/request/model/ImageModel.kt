package com.pictsmanager.request.model

import android.graphics.Bitmap

class ImageModel {
    var name: String? = null
    var access_read: Boolean? = null
    var image: ByteArray? = null

    constructor()

    constructor(_name: String, _access_read: Boolean, _image: ByteArray) {
        name = _name
        access_read = _access_read
        image = _image
    }

    override fun toString(): String {
        return "name:$name\naccess_read:$access_read"
    }
}