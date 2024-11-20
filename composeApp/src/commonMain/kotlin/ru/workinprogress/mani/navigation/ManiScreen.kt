package ru.workinprogress.mani.navigation

enum class ManiScreen {
    Preload,
    Main,
    History,
    Add,
    Edit,
    Login,
    Signup,
}

fun ManiScreen.title() = when (this) {
    ManiScreen.Main -> "Home"
    ManiScreen.Add -> "Add transaction"
    ManiScreen.Edit -> "Edit transaction"
    ManiScreen.History -> "History"
    ManiScreen.Signup -> "Mani"
    else -> ""
}