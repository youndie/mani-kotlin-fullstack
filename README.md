This is a Kotlin Multiplatform budget planner targeting Android, iOS, Web, Server.

* specify server's url in Constants.kt
  `const val BASE_URL = "..."`
* build and dockerize server
`gradle publishImageToLocalRegistry`
* start server with mongodb
`docker-compose up -d`
