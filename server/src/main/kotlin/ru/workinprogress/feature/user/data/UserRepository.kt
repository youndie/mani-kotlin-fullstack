package ru.workinprogress.feature.user.data

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import ru.workinprogress.feature.auth.LoginParams
import ru.workinprogress.mani.model.stringValue
import ru.workinprogress.feature.user.User
import ru.workinprogress.feature.user.data.UserDb.Companion.toUser
import ru.workinprogress.mani.utilz.sha256

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

        return if (entity?.password == credentials.password.sha256()) entity.toUser() else null
    }

    suspend fun findUserById(id: String): User? {
        val entity = db.find<UserDb>(
            Filters.eq("_id", ObjectId(id))
        ).firstOrNull()

        return entity?.toUser()
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
