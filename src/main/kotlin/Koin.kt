package com.example

import com.example.di.appModule
import com.example.repo.DynamoDbUrlRepository
import com.example.repo.UrlRepository
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single{environment.config}
        },
            appModule
        )
    }
}
