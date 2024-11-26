package ru.workinprogress.feature.user.data

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import ru.workinprogress.feature.user.User
import ru.workinprogress.feature.user.data.UserDb.Companion.fromDb

class TokenRepository(private val mongoDatabase: MongoDatabase) {
    private val db get() = mongoDatabase.getCollection<UserDb>(UserRepository.Companion.USER_COLLECTION)

    suspend fun addToken(token: String, userId: String) {
        db.updateOne(
            Filters.eq("_id", ObjectId(userId)),
            Updates.addToSet<String>(UserDb::tokens.name, token),
        )
    }

    suspend fun findUserByToken(refreshToken: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq(UserDb::tokens.name, refreshToken)
        ).firstOrNull()

        return entity?.fromDb()
    }

    suspend fun removeToken(token: String, userId: String) {
        db.updateOne(
            Filters.eq("_id", ObjectId(userId)),
            Updates.pull<String>(UserDb::tokens.name, token),
        )
    }
}