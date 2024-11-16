package ru.workinprogress.feature.user.data

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.mani.model.stringValue
import ru.workinprogress.feature.user.User
import java.security.MessageDigest

class UserRepository(private val mongoDatabase: MongoDatabase) {

    private val db get() = mongoDatabase.getCollection<UserDb>(USER_COLLECTION)

    suspend fun save(user: LoginParams): String? {
        try {
            val result = db.insertOne(
                UserDb(
                    ObjectId(), user.name, user.password.sha256(), emptyList()
                )
            )
            return result.insertedId.stringValue
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
        }
        return null
    }

    suspend fun findUserByCredentials(credentials: LoginParams): User? {
        val entity = db.find<UserDb>(
            Filters.eq("username", credentials.name)
        ).firstOrNull()

        return if (entity?.password == credentials.password.sha256()) entity.let {
            User(it.id.toHexString(), it.username)
        } else null
    }

    suspend fun findUserById(id: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq("_id", ObjectId(id))
        ).firstOrNull()

        return entity?.let {
            User(it.id.toHexString(), it.username)
        }
    }

    suspend fun findUserByToken(refreshToken: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq(UserDb::tokens.name, refreshToken)
        ).firstOrNull()

        return entity?.let {
            User(it.id.toHexString(), it.username)
        }
    }

    suspend fun findByUsername(userName: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq("username", userName)
        ).firstOrNull()

        return entity?.let {
            User(it.id.toHexString(), it.username)
        }
    }

    suspend fun setToken(token: String, userId: String) {
        db.updateOne(
            Filters.eq("_id", ObjectId(userId)),
            Updates.set<List<String>>(UserDb::tokens.name, listOf(token)),
            UpdateOptions().upsert(true)
        )
    }

    companion object {
        const val USER_COLLECTION = "users"
    }
}

fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

@OptIn(ExperimentalStdlibApi::class)
private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .toHexString()
}