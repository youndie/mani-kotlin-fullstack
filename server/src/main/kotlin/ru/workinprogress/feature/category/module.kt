package ru.workinprogress.feature.category

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.workinprogress.feature.category.data.CategoryRepositoryImpl

val categoryModule = module {
    singleOf(::CategoryRepositoryImpl).bind<CategoryRepository>()
}