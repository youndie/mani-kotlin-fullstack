package ru.workinprogress.mani.navigation

enum class ManiScreen {
    Preload,
    Main,
    Add,
    Login,
}

fun ManiScreen.title() = when (this) {
    ManiScreen.Main -> "Home"
    ManiScreen.Add -> "Add transaction"
    ManiScreen.Login -> ""
    ManiScreen.Preload -> ""
}