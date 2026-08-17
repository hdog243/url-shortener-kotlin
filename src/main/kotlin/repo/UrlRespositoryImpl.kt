package com.example.repo

import com.example.models.UrlMappingItem
import com.example.service.UrlAliasGen
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException
import java.util.UUID

class DuplicateAliasException(message: String) : Exception(message)

class DynamoDbUrlRepository(
    private val enhancedClient: DynamoDbEnhancedClient,
    private val tableName: String
) : UrlRepository {

    private val table: DynamoDbTable<UrlMappingItem> = enhancedClient.table(
        tableName,
        TableSchema.fromBean(UrlMappingItem::class.java)
    )

    override suspend fun createShortUrl(fullUrl: String?, customAlias: String?): UrlMappingItem {

        var alias = customAlias
        if(alias == null) {
            alias = UrlAliasGen.createAlias(7)
        }


        val item = UrlMappingItem(
            alias = alias,
            fullUrl = fullUrl
        )

        // Enforce DB-level uniqueness: only put if the partition key (alias) does not exist
        val request = PutItemEnhancedRequest.builder(UrlMappingItem::class.java)
            .item(item)
            .conditionExpression(
                Expression.builder()
                    .expression("attribute_not_exists(alias)")
                    .build()
            )
            .build()

        try {
            table.putItem(request)
            return item
        } catch (ex: ConditionalCheckFailedException) {
            // DynamoDB rejected the write because the alias is already taken
            throw DuplicateAliasException("The alias '$alias' is already in use.")
        }
    }

    override suspend fun getByAlias(alias: String?): UrlMappingItem? {
        val key = Key.builder().partitionValue(alias).build()
        return table.getItem(key) // O(1) point lookup on primary key
    }

    override suspend fun deleteByAlias(alias: String?): Boolean {
        val item = table.deleteItem(Key.builder().partitionValue(alias?.trim('/')).build())
        return item != null
    }

    override suspend fun getAll(): List<UrlMappingItem> {
        try {
            return table.scan()
                .items()
                .toList()
        } catch (ex: DynamoDbException) {
            println("Status Code: ${ex.statusCode()}")
            println("Error Code ${ex.awsErrorDetails().errorCode()}")
            print("Error Message ${ex.awsErrorDetails().errorMessage()}")
            return emptyList()
        }
    }
}