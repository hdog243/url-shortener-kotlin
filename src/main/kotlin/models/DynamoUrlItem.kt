package com.example.models


import com.example.dto.UrlResponseDto
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey

@DynamoDbBean
data class UrlMappingItem(
    @get:DynamoDbPartitionKey
    var alias: String? = null,
    var fullUrl: String? = null,
    var createdAt: Long = System.currentTimeMillis()
){

}
