package com.example.service

import com.sun.jndi.toolkit.url.Uri
import java.net.URI

class UrlValidator {
    companion object {
        suspend fun validate(url: String?): Boolean{
            if (url.isNullOrEmpty()) return false

            val uri = URI(url)
            if(uri.isAbsolute &&
                (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
                !uri.host.isNullOrBlank()
                ) {
                return true;
            }
            return false;
        }
    }
}