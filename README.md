# mani

![Static Badge](https://img.shields.io/badge/Android-green)
![Static Badge](https://img.shields.io/badge/iOS-black)
![Static Badge](https://img.shields.io/badge/Desktop-blue)
![Static Badge](https://img.shields.io/badge/Browser(JS)-orange)
![Static Badge](https://img.shields.io/badge/Server(JVM)-red)

Simple kotlin multiplatform budget planner app. [Ktor](https://ktor.io/)
server + [Compose multiplatform](https://www.jetbrains.com/compose-multiplatform/) clients


![screenshot](/Screenshot.png?raw=true "screenshot")

### demo

https://mani.kotlin.website

also available [swagger ui](https://mani.kotlin.website/swagger/index.html) 

### local run

* configure server config `ru.workinprogress.mani.Constants.kt`

```kotlin
val currentServerConfig: ServerConfig = ServerConfig(
    "Local",
    scheme = "http",
    host = <your ip>,
    development = true,
    port = "8080"
)`
```

* build server
  `gradle publishImageToLocalRegistry`
* start
  `docker-compose up -d`
* web: open http://localhost:8080/


* android:
  `gradle installDebug`
* desktop:
  `gradle desktopRun`
* ios: open iosApp/iosApp.xcodeproj via xcode and run or just [fleet](https://www.jetbrains.com/help/kotlin-multiplatform-dev/fleet.html) 
