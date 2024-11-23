package ru.workinprogress.feature.user.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import ru.workinprogress.feature.user.User

data class UserDb(
    @BsonId val id: ObjectId,
    val username: String,
    val password: String,
    val salt: String?,
    val tokens: List<String>
) {
    companion object {
        fun UserDb.fromDb() = User(this.id.toHexString(), this.username)
    }
}