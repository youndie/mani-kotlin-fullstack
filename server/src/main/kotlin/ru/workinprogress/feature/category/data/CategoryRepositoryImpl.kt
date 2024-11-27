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

    override suspend fun getByUser(userId: String) =
        getUserById(userId)?.categories?.map { db -> mapFromDb(db) }.orEmpty()

    override suspend fun create(category: Category, userId: String): Category {
        val newCategory = CategoryDb(ObjectId(), category.name)

        db.findOneAndUpdate(
            Filters.eq("_id", ObjectId(userId)),
            Updates.addToSet<CategoryDb>(
                UserDb::categories.name, newCategory
            )
        )

        return mapFromDb(newCategory)
    }

    override suspend fun getById(id: String): Category? {
        return getUserById(id)?.categories
            ?.find { category -> category.id.toHexString() == id }
            ?.let { db ->
                mapFromDb(db)
            }
    }

    override suspend fun update(category: Category): Category {
        return db.findOneAndUpdate(
            Filters.eq("categories._id", ObjectId(category.id)),
            Updates.set("categories.name", category.name)
        )?.categories
            ?.find { categoryDb -> categoryDb.id.toHexString() == category.id }
            ?.let { db ->
                mapFromDb(db)
            }!!

    }

    override suspend fun delete(categoryId: String) {
        val user = getUserByCategoryId(categoryId)
        user?.categories
            ?.find { category -> category.id.toHexString() == categoryId }
            ?.let { category ->
                db.updateOne(
                    Filters.eq("_id", user.id),
                    Updates.pull<CategoryDb>(
                        UserDb::categories.name, category
                    )
                )
            }
    }

    private suspend fun getUserById(userId: String) = db.find<UserDb>(Filters.eq("_id", ObjectId(userId))).firstOrNull()

    private suspend fun getUserByCategoryId(categoryId: String) =
        db.find<UserDb>(Filters.eq("categories._id", ObjectId(categoryId))).firstOrNull()

    private fun mapFromDb(db: CategoryDb) = Category(db.id.toHexString(), db.name)

}