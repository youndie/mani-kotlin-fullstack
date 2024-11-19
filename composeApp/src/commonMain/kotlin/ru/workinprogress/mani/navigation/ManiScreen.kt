package ru.workinprogress.mani.navigation

enum class ManiScreen {
    Preload,
    Main,
    History,
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
    ManiScreen.History -> "History"
}