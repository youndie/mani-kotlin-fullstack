package ru.workinprogress.feature.user

import io.ktor.resources.*


@Resource("/users")
class UserResource {

    @Resource("/current")
    class CurrentUserResource(val parent: UserResource) {

        @Resource("/category")
        class CurrentUserCategoryResource(val parent: CurrentUserResource) {

            @Resource("/{id}")
            class ById(val parent: CurrentUserCategoryResource, val id: String)
        }
    }
}

