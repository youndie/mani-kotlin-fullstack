# mani

![Static Badge](https://img.shields.io/badge/Android-green)
![Static Badge](https://img.shields.io/badge/iOS-black)
![Static Badge](https://img.shields.io/badge/Desktop-blue)
![Static Badge](https://img.shields.io/badge/Browser(WASM)-orange)
![Static Badge](https://img.shields.io/badge/Server(JVM)-red)

A modern Kotlin multiplatform budget planner application built with [Ktor](https://ktor.io/) for the backend
and [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) for clients.

![Screenshot](/Screenshot.png?raw=true "screenshot")

### Demo

Check out live demo here: [mani.kotlin.website](https://mani.kotlin.website)  

### Running Locally

1. Configure server settings in `ru.workinprogress.mani.Constants.kt`:

   ```kotlin
   val currentServerConfig: ServerConfig = ServerConfig(
       name = "Local",
       scheme = "http",
       host = "<your-ip>",
       development = true,
       port = "8080"
   )
   ```

2. Build the server:

   ```bash
   ./gradlew publishImageToLocalRegistry
   ```

3. Start the server using Docker:

   ```bash
   docker-compose up -d
   ```

4. Access the web application at [http://localhost:8080/](http://localhost:8080/).

---

#### Running on Different Platforms:

- **Android:**  
  Build and install with:
  ```bash
  ./gradlew installDebug
  ```

- **Desktop:**  
  Run the app on desktop with:
  ```bash
  ./gradlew desktopRun
  ```

- **iOS:**  
  Open `iosApp/iosApp.xcodeproj` in Xcode and run the application, or
  use [Fleet](https://www.jetbrains.com/help/kotlin-multiplatform-dev/fleet.html) for development.
