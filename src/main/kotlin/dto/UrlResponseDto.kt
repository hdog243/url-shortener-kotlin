package com.example.dto

import com.example.models.UrlMappingItem
import kotlinx.serialization.Serializable

@Serializable
data class UrlResponseDto(
    val alias :String,
    val fullUrl :String,
    val createdAt: Long
)
fun UrlMappingItem.toDto(): UrlResponseDto = UrlResponseDto(
    alias = alias.orEmpty(),
    fullUrl = fullUrl.orEmpty(),
    createdAt = createdAt
)