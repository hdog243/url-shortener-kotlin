package com.example.routes

import com.example.repo.UrlRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import org.koin.ktor.ext.inject

//returns 204 on success, 404 otherwise
fun Routing.deleteByAlias() {
    delete("/{alias}") {

        val alias = call.parameters["alias"]
        val urlRepo : UrlRepository by inject()

        if(urlRepo.deleteByAlias(alias)) {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}