package com.example.routes

import com.example.models.ShortenRequest
import com.example.models.ShortenResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post


fun Route.shorten() {
    post("/shorten") {
        try {
            val request = call.receive<ShortenRequest>()
            call.response.status(HttpStatusCode.Created)
            //val shortenResponse : ShortenResponse = ShortenResponse("")
            // call.respond(shortenResponse)
        }
        catch(e: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}