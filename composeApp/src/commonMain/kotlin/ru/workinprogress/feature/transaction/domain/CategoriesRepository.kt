package ru.workinprogress.feature.transaction.domain

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.workinprogress.feature.category.CategoryResource
import ru.workinprogress.feature.transaction.BaseFlowRepository
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.DataSource

class CategoriesNetworkDataSource(private val httpClient: HttpClient) : DataSource<Category> {
    override suspend fun create(params: Category): Category {
        return httpClient.post(CategoryResource()) {
            setBody(params)
        }.body()
    }

    override suspend fun load(): List<Category> {
        return httpClient.get(CategoryResource()).body()
    }

    override suspend fun update(params: Category): Category? {
        return httpClient.patch(CategoryResource.ById(id = params.id)) {
            setBody(params)
        }.takeIf { it.status == HttpStatusCode.OK }?.body()
    }

    override suspend fun delete(id: String): Boolean {
        return httpClient.delete(CategoryResource.ById(id = id)).status == HttpStatusCode.OK
    }
}

class CategoriesRepository(dataSource: CategoriesNetworkDataSource) : BaseFlowRepository<Category>(dataSource)