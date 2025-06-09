package com.hadeedahyan.sliideandroidapp.domain.model

data class User (
    val id: Int,
    val name: String,
    val email: String,
    val gender: String,
    val status: String,
    val createdAt: Long? = null // Cl ient-siiide timestamp in milliseconds
)