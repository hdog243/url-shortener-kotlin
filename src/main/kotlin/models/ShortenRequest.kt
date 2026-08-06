package com.example.models

import kotlinx.serialization.Serializable

@Serializable
class ShortenRequest (
    val fullUrl: String,
    val customAlias :String? = null
)