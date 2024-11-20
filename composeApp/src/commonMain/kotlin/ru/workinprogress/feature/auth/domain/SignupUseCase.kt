package ru.workinprogress.feature.auth.domain

import ru.workinprogress.feature.auth.LoginParams

interface UserService {
    suspend fun signup(params: LoginParams): Boolean
}

class SignupUseCase(private val userService: UserService) : AuthUseCase() {
    override suspend fun invoke(params: LoginParams): Result<Boolean> {
        return withTry {
            userService.signup(params)
            true
        }
    }
}