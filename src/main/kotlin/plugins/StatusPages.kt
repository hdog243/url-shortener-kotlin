package com.example.plugins

import com.example.repo.DuplicateAliasException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
}
