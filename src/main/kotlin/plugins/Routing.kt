package com.example.plugins

import com.example.routes.deleteByAlias
import com.example.routes.getAlias
import com.example.routes.getAllUrls
import com.example.routes.shorten
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        getAllUrls()
        shorten()
        getAlias()
        deleteByAlias()

    }
}