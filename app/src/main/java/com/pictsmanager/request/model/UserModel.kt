package com.pictsmanager.request.model

class UserModel {
    var email: String? = null
    var password: String? = null

    override fun toString(): String {
        return "Email:$email\nPass:$password"
    }
}