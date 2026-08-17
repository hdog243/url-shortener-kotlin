package com.example.service

import java.security.SecureRandom

class UrlAliasGen {

    companion object {
        private const val BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val random = SecureRandom() // Thread-safe, non-blocking random generator


        fun createAlias(length: Int?): String {
            var aliasLength = length

            //if length is not specified then we'll default to 7
            //we should have a min requirement here but not needed right now
            if (aliasLength == null)
                aliasLength = 7

            return String(CharArray(aliasLength) {
                BASE62_CHARS[random.nextInt(BASE62_CHARS.length)]
            })
        }
    }
}