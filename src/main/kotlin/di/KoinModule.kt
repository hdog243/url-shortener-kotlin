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

        fun getConfigOrEnv(configPath: String, vararg envVars: String): String? {
            val fromConfig = runCatching {
                config.propertyOrNull(configPath)?.getString()?.takeIf { it.isNotBlank() }
            }.getOrNull()

            if (fromConfig != null) return fromConfig

            return envVars.firstNotNullOfOrNull { envVar ->
                System.getenv(envVar)?.takeIf { it.isNotBlank() }
            }
        }

        val regionStr = getConfigOrEnv("aws.dynamodb.region", "AWS_REGION", "AWS_DEFAULT_REGION") ?: "eu-west-2"
        val endpointUrl = getConfigOrEnv("aws.dynamodb.endpoint", "DYNAMODB_ENDPOINT")

        println(">>> DYNAMO REGION: '$regionStr'")
        println(">>> DYNAMO ENDPOINT: '${endpointUrl ?: "AWS Native"}'")

        val clientBuilder = DynamoDbClient.builder()
            .region(Region.of(regionStr))

        if (!endpointUrl.isNullOrBlank()) {
            // Local DynamoDB Container Configuration
            clientBuilder
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("dummyAccessKey", "dummySecretKey")
                    )
                )
        } else {
            // Live AWS Fargate / Cloud Configuration
            // DefaultCredentialsProvider automatically checks ECS Task Role credentials
            clientBuilder.credentialsProvider(DefaultCredentialsProvider.create())
        }

        val standardClient = clientBuilder.build()

        DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standardClient)
            .build()
    }

    single<UrlRepository> {
        val config = get<ApplicationConfig>()

        fun getConfigOrEnv(configPath: String, vararg envVars: String): String? {
            val fromConfig = runCatching {
                config.propertyOrNull(configPath)?.getString()?.takeIf { it.isNotBlank() }
            }.getOrNull()

            if (fromConfig != null) return fromConfig

            return envVars.firstNotNullOfOrNull { envVar ->
                System.getenv(envVar)?.takeIf { it.isNotBlank() }
            }
        }

        // Checks config, then DYNAMODB_TABLE (from main.tf), then DYNAMO_TABLE_NAME, then falls back to "UrlMappings"
        val tableName = getConfigOrEnv("aws.dynamodb.tableName", "DYNAMODB_TABLE", "DYNAMO_TABLE_NAME", "DYNAMODB_TABLE_NAME") ?: "UrlMappings"

        println(">>> DYNAMO TABLE NAME: '$tableName'")

        DynamoDbUrlRepository(
            enhancedClient = get(),
            tableName = tableName
        )
    }
}