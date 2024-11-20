plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.pluginSerialization)
    application
}

val appName = "mani-backend"

group = "ru.workinprogress.mani"
version = "0.0.1"

application {
    mainClass.set("ru.workinprogress.mani.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.swagger)
    implementation(libs.swagger.parser)
    implementation(libs.ktor.swagger.ui)
    implementation(libs.slf4j.api)

    implementation(libs.mongodb.driver.kotlin.coroutine)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

//    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.koin.test)

}

val copyFrontend = task<Copy>("copyFrontend") {
    val jsBrowserDistribution =
        project(rootProject.projects.composeApp.path).tasks.named("wasmJsBrowserDevelopmentExecutableDistribution")
    from(jsBrowserDistribution)
    include("styles.css", "composeApp.js", "index.html", "**.wasm", "composeResources/**/*")
    destinationDir = file("$projectDir/build/resources/main/static")
}

project.tasks.find { "processResources" == it.name }!!.dependsOn(copyFrontend)
