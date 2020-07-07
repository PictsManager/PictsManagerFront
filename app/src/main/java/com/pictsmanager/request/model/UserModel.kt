package com.pictsmanager.request.model

class UserModel(var email: String, var password: String) {

    override fun toString(): String {
        return "Email:$email\nPass:$password"
    }
}