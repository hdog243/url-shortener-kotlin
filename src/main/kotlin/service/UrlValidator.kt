package com.example.service

import java.net.URI

class UrlValidator {
    companion object {
        fun validate(url: String?): Boolean{
            if (url.isNullOrEmpty()) return false

            return runCatching{
                val uri = URI(url)
                uri.isAbsolute &&
                (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
                !uri.host.isNullOrBlank()
            }.getOrDefault(false)
        }
    }
}