package ru.workinprogress.feature.transaction.domain

import kotlinx.coroutines.flow.Flow
import ru.workinprogress.feature.auth.domain.ServerException
import ru.workinprogress.feature.transaction.Category
import ru.workinprogress.useCase.EmptyParams
import ru.workinprogress.useCase.NonParameterizedUseCase
import ru.workinprogress.useCase.UseCase

class AddCategoryUseCase(private val repository: CategoriesRepository) : UseCase<Category, Boolean>() {
    override suspend operator fun invoke(params: Category): Result<Boolean> {
        return withTry { repository.create(params) }
    }
}

class GetCategoriesUseCase(
    private val repository: CategoriesRepository,
) : NonParameterizedUseCase<Flow<List<Category>>>() {

    override suspend fun invoke(params: EmptyParams): Result<Flow<List<Category>>> {
        try {
            repository.load()
        } catch (e: Exception) {
            return Result.Error(ServerException("Network Error", e))
        }

        return Result.Success(repository.dataStateFlow)
    }
}

class DeleteCategoryUseCase(private val repository: CategoriesRepository) : UseCase<Category, Boolean>() {
    override suspend fun invoke(params: Category): Result<Boolean> {
        return withTry {
            repository.delete(params.id)
        }
    }
}