package com.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.getAlias() {
    get("/{alias}") {
        val alias = call.parameters["alias"]


        call.response.status(HttpStatusCode.PermanentRedirect)
//        TODO("build in repo stuff")
//        TODO("if alias not found return 404")
    }
}