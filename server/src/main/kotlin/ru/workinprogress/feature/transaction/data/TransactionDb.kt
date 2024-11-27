package ru.workinprogress.feature.transaction.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class TransactionDb(
    @BsonId val id: ObjectId,
    val amount: Double,
    val income: Boolean,
    val date: String,
    val until: String?,
    val period: String,
    val comment: String,
    val userId: String,
    val categoryId: String?
)

