package com.example.routes

import com.example.dto.toDto
import com.example.repo.UrlRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.getByAlias() {
    get("/{alias}") {
        val alias = call.parameters["alias"]
        val urlRepo : UrlRepository by inject()

        try {
            val response = urlRepo.getByAlias(alias)

            if(response != null){
                call.respondRedirect(response.fullUrl.toString())
            }
            else
                call.response.status(HttpStatusCode.NotFound)
        }
        catch (e: Exception){
            e.printStackTrace()
            call.response.status(HttpStatusCode.InternalServerError)
        }
    }
}