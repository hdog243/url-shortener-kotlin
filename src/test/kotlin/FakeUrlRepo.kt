package com.example

import com.example.models.UrlMappingItem
import com.example.repo.UrlRepository
import com.example.service.UrlAliasGen
import kotlin.time.Clock

class FakeUrlRepository : UrlRepository {
    private val storage = mutableMapOf<String, UrlMappingItem>()

    fun clear() = storage.clear()

    fun seed(alias: String, fullUrl: String) {
        storage[alias] = UrlMappingItem(
            alias = alias,
            fullUrl = fullUrl,
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun getAll(): List<UrlMappingItem> = storage.values.toList()

    override suspend fun createShortUrl(
        fullUrl: String?,
        customAlias: String?
    ): UrlMappingItem {

        if(customAlias == null) {
            val alias = UrlAliasGen.createAlias(7)

        }

        return UrlMappingItem(
            alias = customAlias,
            fullUrl = fullUrl,
            createdAt = Clock.System.now().epochSeconds
        )
    }

    override suspend fun getByAlias(alias: String?): UrlMappingItem? = storage[alias]

    suspend fun save(mapping: UrlMappingItem) {
        storage[mapping.alias as String] = mapping
    }

    override suspend fun deleteByAlias(alias: String?): Boolean {
        return storage.remove(alias) != null
    }
}