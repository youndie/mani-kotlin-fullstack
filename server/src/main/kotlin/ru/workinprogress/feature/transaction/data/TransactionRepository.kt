package ru.workinprogress.feature.transaction.data

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDate
import org.bson.types.ObjectId
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.mani.db.deleteById
import ru.workinprogress.mani.model.stringValue

class TransactionRepository(mongoDatabase: MongoDatabase) {

    private val db = mongoDatabase.getCollection<TransactionDb>("transaction")

    suspend fun create(transaction: Transaction, userId: String): String {
        return db.insertOne(
            mapToDb(transaction, userId = userId)
        ).insertedId.stringValue
    }

    suspend fun getById(id: String): TransactionDb? {
        return db.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    }

    suspend fun getByUser(userId: String): List<TransactionDb> {
        return db.find<TransactionDb>(Filters.eq(TransactionDb::userId.name, userId)).toList()
    }

    suspend fun update(transaction: Transaction, userId: String) {
        val id = ObjectId(transaction.id)
        db.replaceOne(
            Filters.eq("_id", id), mapToDb(transaction, id, userId)
        )
    }

    suspend fun delete(id: String): Boolean {
        return db.deleteById(id)
    }

    companion object {

        private fun mapToDb(transaction: Transaction, id: ObjectId = ObjectId(), userId: String) =
            TransactionDb(
                id = id,
                amount = transaction.amount,
                income = transaction.income,
                date = transaction.date.toString(),
                until = transaction.until?.toString(),
                period = transaction.period.name,
                comment = transaction.comment,
                userId = userId,
                categoryId = transaction.category.id
            )

        fun TransactionDb.mapFromDb(userCategories: List<Category>): Transaction {
            return Transaction(
                id = id.toHexString(),
                amount = amount,
                income = income,
                date = LocalDate.Companion.parse(date),
                period = Transaction.Period.valueOf(period),
                until = until?.let(LocalDate.Companion::parse),
                comment = comment,
                category = userCategories.find { category -> category.id == categoryId } ?: Category.default
            )
        }
    }
}