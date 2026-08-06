package com.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.getAllUrls() {
    get("/urls") {
        call.response.status(HttpStatusCode.OK)
    }
}