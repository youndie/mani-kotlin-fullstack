package ru.workinprogress.feature.user.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserDb(
    @BsonId val id: ObjectId,
    val username: String,
    val password: String,
    val tokens: List<String>
)