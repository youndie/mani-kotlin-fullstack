package ru.workinprogress.feature.categories.data

import ru.workinprogress.feature.transaction.BaseFlowRepository
import ru.workinprogress.feature.transaction.Category

class CategoriesRepository(dataSource: CategoriesNetworkDataSource) : BaseFlowRepository<Category>(dataSource)