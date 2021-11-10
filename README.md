# async-service
Quick proof-of-concept on preserving Thread Local Storage when making async calls from a service.

# Compile
Run `./gradlew.bat clean build` on Windows, or equivalent on macOS.

This will produce a WAR file at `build\libs`

# Run it
To start the service as a stand-alone Spring-Boot service type `java -jar .\build\libs\async-service-0.0.1-SNAPSHOT.war`

# Try it Out
Open a web browser and navigate to http://localhost:8083/api/ping

NOTE: That is port 8083, not the usual port 8080.

You should get back a response that says "pong".
