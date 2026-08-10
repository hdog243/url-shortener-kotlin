package com.example.di

import com.example.repo.DynamoDbUrlRepository
import com.example.repo.UrlRepository
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

val appModule = module {

    single<DynamoDbEnhancedClient> {
        val config = get<ApplicationConfig>()

        fun getConfigOrEnv(configPath: String, envVar: String): String? {
            val fromConfig = try {
                config.propertyOrNull(configPath)?.getString()?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
            val fromEnv = System.getenv(envVar)?.takeIf { it.isNotBlank() }
            return fromConfig ?: fromEnv
        }

        val regionStr = getConfigOrEnv("aws.dynamodb.region", "AWS_REGION") ?: "us-east-1"
        val endpointUrl = getConfigOrEnv("aws.dynamodb.endpoint", "DYNAMODB_ENDPOINT")

        println(">>> DYNAMO REGION: '$regionStr'")
        println(">>> DYNAMO ENDPOINT: '$endpointUrl'")

        val clientBuilder = DynamoDbClient.builder()
            .region(Region.of(regionStr))

        if (!endpointUrl.isNullOrBlank()) {
            clientBuilder
                .endpointOverride(URI.create(endpointUrl))
                // Explicitly set static credentials
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummyAccessKey", "dummySecretKey")
                    )
                )
        } else {
            // Standard cloud credentials provider when endpointUrl is not set
            clientBuilder.credentialsProvider(DefaultCredentialsProvider.create())
        }

        val standardClient = clientBuilder.build()

        DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standardClient)
            .build()
    }

    single<UrlRepository> {
        val config = get<ApplicationConfig>()
        val tableName = config.propertyOrNull("aws.dynamodb.tableName")?.getString()?.takeIf { it.isNotBlank() }
            ?: System.getenv("DYNAMO_TABLE_NAME")?.takeIf { it.isNotBlank() }
            ?: System.getenv("DYNAMODB_TABLE_NAME")?.takeIf { it.isNotBlank() }
            ?: "urls"

        println(">>> table: '$tableName'")
        DynamoDbUrlRepository(
            enhancedClient = get(),
            tableName = tableName
        )
    }
}