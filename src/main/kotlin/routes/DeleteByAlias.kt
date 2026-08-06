package com.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete

//returns 204 on success, 404 otherwise
fun Routing.deleteByAlias() {
    delete("/{alias}") {
        val alias = call.parameters["alias"]

        //TODO("Repo commands, respond 204 if found, 404 if not found")

        call.response.status(HttpStatusCode.NoContent)

    }
}