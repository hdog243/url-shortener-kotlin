package com.example.plugins

import com.example.routes.deleteByAlias
import com.example.routes.getByAlias
import com.example.routes.getAllUrls
import com.example.routes.shorten
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        getAllUrls()
        shorten()
        getByAlias()
        deleteByAlias()

    }
}