# mani

Simple kotlin multiplatform budget planner app. [Ktor](https://ktor.io/)
server + [Compose multiplatform](https://www.jetbrains.com/compose-multiplatform/) clients

![Static Badge](https://img.shields.io/badge/Android-green)
![Static Badge](https://img.shields.io/badge/iOS-black)
![Static Badge](https://img.shields.io/badge/Desktop-blue)
![Static Badge](https://img.shields.io/badge/Browser(JS)-orange)
![Static Badge](https://img.shields.io/badge/Server(JVM)-red)

![screenshot](/Screenshot.png?raw=true "Optional Title")

### demo

https://mani.kotlin.website

### run
* build server
  `gradle publishImageToLocalRegistry`
* start
  `docker-compose up -d`
* web: open http://localhost:8080/


* android
  `gradle installDebug`
* desktop
  `gradle desktopRun`
* ios: open iosApp/iosApp.xcodeproj via xcode and run
