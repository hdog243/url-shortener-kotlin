package com.example.repo

import com.example.models.UrlMappingItem


    interface UrlRepository{
        suspend fun createShortUrl(fullUrl: String?, customAlias: String? = null): UrlMappingItem
        suspend fun getByAlias(alias: String?): UrlMappingItem?
        suspend fun deleteByAlias(alias: String?): Boolean
        suspend fun getAll():List<UrlMappingItem>
    }
