kotlin multiplatform budget planner 

desktop, android, ios, wasmjs + ktor server

![screenshot](/Screenshot.png?raw=true "Optional Title")

demo: https://mani.kotlin.website/


run:
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
