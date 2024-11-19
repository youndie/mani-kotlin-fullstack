package ru.workinprogress.mani.navigation

enum class ManiScreen {
    Preload,
    Main,
    Add,
    Edit,
    Login,
}

fun ManiScreen.title() = when (this) {
    ManiScreen.Main -> "Home"
    ManiScreen.Add -> "Add transaction"
    ManiScreen.Edit -> "Edit transaction"
    ManiScreen.Login -> ""
    ManiScreen.Preload -> ""
}