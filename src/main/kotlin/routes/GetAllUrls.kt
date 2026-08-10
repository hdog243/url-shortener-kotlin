package com.example.routes

import com.example.dto.toDto
import com.example.repo.UrlRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException

fun Route.getAllUrls() {
    get("/urls") {
        try{
        val urlRepo: UrlRepository by inject()

            val allUrls = urlRepo.getAll()
            //map this to a dto because the serializer has trouble with the Dynamo object
            val dtoList = allUrls.map {it.toDto()}
            call.respond(HttpStatusCode.OK, dtoList)

        }
        catch (dynamoException: DynamoDbException){
            println("error code: ${dynamoException.awsErrorDetails().errorCode()}")
            println("error message : ${dynamoException.awsErrorDetails().errorMessage()}")
            call.respond(HttpStatusCode.InternalServerError, dynamoException.awsErrorDetails().errorMessage())
        }
        catch(e: Exception){
            e.printStackTrace()
            call.response.status(HttpStatusCode.InternalServerError)
        }


    }
}