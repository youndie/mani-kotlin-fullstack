package ru.workinprogress.feature.category.data

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import ru.workinprogress.feature.category.CategoryRepository
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.data.CategoryDb
import ru.workinprogress.feature.user.data.UserDb
import ru.workinprogress.feature.user.data.UserRepository

class CategoryRepositoryImpl(mongoDatabase: MongoDatabase) : CategoryRepository {

    private val db = mongoDatabase.getCollection<UserDb>(UserRepository.Companion.USER_COLLECTION)

    private suspend fun getUserById(userId: String) = db.find<UserDb>(Filters.eq("_id", ObjectId(userId))).firstOrNull()

    private fun mapFromDb(db: CategoryDb) = Category(db.id.toHexString(), db.name)

    override suspend fun getByUser(userId: String) =
        getUserById(userId)?.categories?.map { db -> mapFromDb(db) }.orEmpty()

    override suspend fun create(category: Category, userId: String): String {
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

    override suspend fun update(category: Category) {
        db.updateOne(
            Filters.eq(UserDb::categories.name, ObjectId(category.id)),
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