package ru.workinprogress.feature.categories

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.categories.data.CategoriesNetworkDataSource
import ru.workinprogress.feature.categories.data.CategoriesRepository
import ru.workinprogress.feature.categories.domain.AddCategoryUseCase
import ru.workinprogress.feature.categories.domain.DeleteCategoryUseCase
import ru.workinprogress.feature.categories.domain.GetCategoriesUseCase
import ru.workinprogress.feature.categories.domain.ObserveCategoriesUseCase
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.feature.transaction.DataSource

val categoriesModule = module {
    singleOf(::AddCategoryUseCase)
    singleOf(::DeleteCategoryUseCase)
    singleOf(::GetCategoriesUseCase)
    singleOf(::CategoriesNetworkDataSource).bind<DataSource<Category>>()
    singleOf(::CategoriesRepository)
    singleOf(::ObserveCategoriesUseCase)
}