package ru.workinprogress.feature.category

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.delete
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.get
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.patch
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.data.CategoryDb
import ru.workinprogress.feature.user.UserResource
import ru.workinprogress.feature.user.currentUserId
import ru.workinprogress.feature.user.data.UserDb
import ru.workinprogress.feature.user.data.UserRepository
import ru.workinprogress.mani.model.JWTConfig

val categoryModule = module {
    singleOf(::CategoryRepositoryImpl).bind<CategoryRepository>()
}


class CategoryRepositoryImpl(mongoDatabase: MongoDatabase) : CategoryRepository {

    private val db = mongoDatabase.getCollection<UserDb>(UserRepository.Companion.USER_COLLECTION)

    private suspend fun getUserById(userId: String) = db.find<UserDb>(Filters.eq("_id", ObjectId(userId))).firstOrNull()

    private fun mapFromDb(db: CategoryDb) = Category(db.id.toHexString(), db.name)

    override suspend fun getByUser(userId: String) =
        getUserById(userId)?.categories?.map { db -> mapFromDb(db) }.orEmpty()

    override suspend fun create(
        category: Category, userId: String
    ): String {
        val newCategory = CategoryDb(
            ObjectId(), category.name
        )

        db.updateOne(
            Filters.eq("_id", ObjectId(userId)), Updates.addToSet<CategoryDb>(
                UserDb::categories.name, newCategory
            )
        )

        return newCategory.id.toHexString()
    }

    override suspend fun getById(id: String): Category? {
        return db.find<CategoryDb>(Filters.eq(UserDb::categories.name, ObjectId(id))).firstOrNull()?.let { db ->
            mapFromDb(db)
        }
    }

    override suspend fun update(categoryId: String, category: Category) {
        db.updateOne(
            Filters.eq(UserDb::categories.name, ObjectId(categoryId)),
            Updates.set(
                CategoryDb::name.name, category.name
            )
        )
    }


    override suspend fun delete(categoryId: String, userId: String) {
        db.find<CategoryDb>(Filters.eq(UserDb::categories.name, ObjectId(categoryId))).firstOrNull()?.let { category ->
            db.updateOne(
                Filters.eq("_id", ObjectId(userId)), Updates.pull<CategoryDb>(
                    UserDb::categories.name, category
                )
            )
        }
    }
}

interface CategoryRepository {
    suspend fun getByUser(userId: String): List<Category>
    suspend fun create(category: Category, userId: String): String
    suspend fun getById(string: String): Category?
    suspend fun update(categoryId: String, category: Category)
    suspend fun delete(categoryId: String, userId: String)
}

fun Routing.categoryRouting() {
    val jwtConfig by inject<JWTConfig>()
    val categoryRepository by inject<CategoryRepository>()

    authenticate(jwtConfig.name) {
        get<UserResource.CurrentUserResource.CurrentUserCategoryResource>({
            response { HttpStatusCode.OK to { body<List<Category>>() } }
        }) {
            call.respond(
                categoryRepository.getByUser(call.currentUserId())
            )
        }

        post<UserResource.CurrentUserResource.CurrentUserCategoryResource>({
            request {
                body<Category>()
            }
            response { HttpStatusCode.Created to { body<Category>() } }
        }) {
            categoryRepository.create(call.receive<Category>(), call.currentUserId()).let {
                categoryRepository.getById(it)
            }?.let { category ->
                call.respond(category)
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        get<UserResource.CurrentUserResource.CurrentUserCategoryResource.ById>({
            request {
                pathParameter<String>("id")
            }
            response { HttpStatusCode.OK to { body<Category>() } }
        }) { path ->
            categoryRepository.getById(path.id)?.let { category ->
                call.respond(category)
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        patch<UserResource.CurrentUserResource.CurrentUserCategoryResource.ById>({
            request {
                pathParameter<String>("id")
                body<Category>()
            }
            response { HttpStatusCode.OK to { body<Category>() } }
        }) { path ->
            categoryRepository.update(path.id, call.receive<Category>())

            categoryRepository.getById(path.id)?.let { category ->
                call.respond(category)
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        delete<UserResource.CurrentUserResource.CurrentUserCategoryResource.ById>({
            request {
                pathParameter<String>("id")
            }
            response { HttpStatusCode.OK to { } }
        }) { path ->
            if (categoryRepository.getByUser(call.currentUserId()).none { category -> category.id == path.id }) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }

            categoryRepository.delete(call.currentUserId(), path.id)
        }
    }
}