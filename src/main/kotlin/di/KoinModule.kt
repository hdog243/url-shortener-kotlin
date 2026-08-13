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

        // Prioritizes Environment Variables over application.conf / application.yaml
        fun getEnvOrConfig(configPath: String, vararg envVars: String): String? {
            val fromEnv = envVars.firstNotNullOfOrNull { envVar ->
                System.getenv(envVar)?.takeIf { it.isNotBlank() }
            }
            if (fromEnv != null) return fromEnv

            return runCatching {
                config.propertyOrNull(configPath)?.getString()?.takeIf { it.isNotBlank() }
            }.getOrNull()
        }

        val regionStr = getEnvOrConfig("aws.dynamodb.region", "AWS_REGION", "AWS_DEFAULT_REGION") ?: "eu-west-2"
        val endpointUrl = getEnvOrConfig("aws.dynamodb.endpoint", "DYNAMODB_ENDPOINT")

        println(">>> DYNAMO REGION: '$regionStr'")
        println(">>> DYNAMO ENDPOINT: '${endpointUrl ?: "AWS Native"}'")

        val clientBuilder = DynamoDbClient.builder()
            .region(Region.of(regionStr))

        if (!endpointUrl.isNullOrBlank()) {
            // Local DynamoDB Container Configuration (triggers only when DYNAMODB_ENDPOINT is explicitly set)
            clientBuilder
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummyAccessKey", "dummySecretKey")
                    )
                )
        } else {
            // Live AWS Fargate / Cloud Configuration
            // Uses ECS Task Role temporary credentials automatically
            clientBuilder.credentialsProvider(DefaultCredentialsProvider.create())
        }

        val standardClient = clientBuilder.build()

        DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standardClient)
            .build()
    }

    single<UrlRepository> {
        val config = get<ApplicationConfig>()

        fun getEnvOrConfig(configPath: String, vararg envVars: String): String? {
            val fromEnv = envVars.firstNotNullOfOrNull { envVar ->
                System.getenv(envVar)?.takeIf { it.isNotBlank() }
            }
            if (fromEnv != null) return fromEnv

            return runCatching {
                config.propertyOrNull(configPath)?.getString()?.takeIf { it.isNotBlank() }
            }.getOrNull()
        }

        val tableName = getEnvOrConfig("aws.dynamodb.tableName", "DYNAMODB_TABLE", "DYNAMO_TABLE_NAME", "DYNAMODB_TABLE_NAME") ?: "UrlMappings"

        println(">>> DYNAMO TABLE NAME: '$tableName'")

        DynamoDbUrlRepository(
            enhancedClient = get(),
            tableName = tableName
        )
    }
}