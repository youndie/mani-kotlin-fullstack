package ru.workinprogress.feature.category

import io.ktor.resources.*

@Resource("/category")
class CategoryResource {

    @Resource("/{id}")
    class ById(val parent: CategoryResource, val id: String)
}