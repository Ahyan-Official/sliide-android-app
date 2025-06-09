package com.hadeedahyan.sliideandroidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto (
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("status") val status: String
)