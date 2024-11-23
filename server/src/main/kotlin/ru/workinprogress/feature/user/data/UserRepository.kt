package ru.workinprogress.feature.user.data

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.feature.user.User
import ru.workinprogress.feature.user.data.UserDb.Companion.fromDb
import ru.workinprogress.mani.model.stringValue
import ru.workinprogress.feature.auth.data.hashing.HashingService
import ru.workinprogress.feature.auth.data.hashing.SaltedHash

class UserRepository(private val mongoDatabase: MongoDatabase, private val hashingService: HashingService) {

    private val db get() = mongoDatabase.getCollection<UserDb>(USER_COLLECTION)

    suspend fun save(user: LoginParams): String? {
        try {
            val saltedHash = hashingService.generateSaltedHash(user.password)
            val result = db.insertOne(
                UserDb(
                    ObjectId(), user.name, saltedHash.hash, saltedHash.salt, emptyList()
                )
            )
            return result.insertedId.stringValue
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
        }
        return null
    }

    suspend fun findUserByCredentials(credentials: LoginParams): User? {
        return db.find<UserDb>(Filters.eq("username", credentials.name))
            .firstOrNull()
            ?.let { user ->
                if (hashingService.verify(
                        credentials.password,
                        SaltedHash(user.password, user.salt.orEmpty())
                    )
                ) user.fromDb() else null
            }
    }

    suspend fun findUserById(id: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq("_id", ObjectId(id))
        ).firstOrNull()

        return entity?.fromDb()
    }

    suspend fun findByUsername(userName: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq("username", userName)
        ).firstOrNull()

        return entity?.let {
            User(it.id.toHexString(), it.username)
        }
    }

    companion object {
        const val USER_COLLECTION = "users"
    }
}
