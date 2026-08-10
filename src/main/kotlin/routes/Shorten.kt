package com.example.routes

import com.example.models.ShortenRequest
import com.example.repo.UrlRepository
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject


fun Route.shorten() {
    post("/shorten") {
        try {
            val request = call.receive<ShortenRequest>()

            val urlRepo : UrlRepository by inject()

            //do db insert which tells us if there is a collision
            //because it is a user supplied alias we can one shot this and respond to the user to say they can't have that alias
            val dbResponse = urlRepo.createShortUrl(fullUrl = request.fullUrl, customAlias = request.customAlias)
            if(dbResponse.alias != null){
                call.response.status(HttpStatusCode.Created)
                call.respondText(dbResponse.fullUrl.toString(), ContentType.Text.Plain)
            }
            else{
                call.respond(HttpStatusCode.InternalServerError, message = "Invalid alias or already taken")
            }

            call.response.status(HttpStatusCode.Created)
        }
        catch(e: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}