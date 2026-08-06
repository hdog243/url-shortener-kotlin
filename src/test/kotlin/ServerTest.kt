package com.example

import com.example.models.ShortenRequest
import com.example.models.ShortenResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import io.netty.handler.codec.DefaultHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ServerTest {

    @Test
    fun `get all urls returns success`() = testApplication {
        application{
            module()
        }

        val client = createClient{
            install(ContentNegotiation) {
                json()
            }
        }

        assertEquals(HttpStatusCode.OK, client.get("/urls").status)
    }

    @Test
    fun `delete alias returns no content` () = testApplication {
        application{
            module()
        }

        val client = createClient{
            install(ContentNegotiation) {
                json()
            }
        }

        assertEquals(HttpStatusCode.NoContent, client.delete("/alias").status)
    }

    @Test
    fun `get alias returns redirect` () = testApplication {
        application{
            module()
        }

        val client = createClient{
            install(ContentNegotiation) {
                json()
            }
        }

        assertEquals(HttpStatusCode.PermanentRedirect, client.get("/randomAlias").status)
    }

    @Test
    fun `post to shorten with no body get bad content`() = testApplication {
        application{
            module()
        }

        val client = createClient{
            install(ContentNegotiation) {
                json()
            }
        }
        assertEquals(HttpStatusCode.BadRequest, client.post ( "/shorten" ).status )
    }

    @Test
    fun `post to shorten and respond created`() = testApplication {
        // 1. Configure the test server
        application {
            module()
        }


        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val requestBody = ShortenRequest(fullUrl = "www.google.com", customAlias = "test")


        val response = client.post("/shorten") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

//    @Test
//    fun `post to shorten and returns ShortenResponse object`() = testApplication {
//        application{
//            module()
//        }
//
//        val client = createClient {
//            install(ContentNegotiation) {
//                json()
//            }
//        }
//
//        val requestBody = ShortenRequest(fullUrl = "www.google.com", customAlias = "test")
//
//
//        val response = client.post("/shorten") {
//            contentType(ContentType.Application.Json)
//            setBody(requestBody)
//        }
//
//        val obj = response.body()  as ShortenResponse
//        assertNotEquals("", obj.toString())
//    }
}
