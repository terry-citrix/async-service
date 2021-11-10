# Async Service
A small proof-of-concept on preserving Thread Local Storage when making async calls from a java service.

## Compile It
Run `./gradlew.bat clean build` on Windows, or equivalent on macOS.

This will produce a WAR file at `build\libs`

## Run it
To start the service as a stand-alone Spring-Boot service type `java -jar .\build\libs\async-service-0.0.1-SNAPSHOT.war`

## Sanity Check
If you ran it as a stand-alone Spring-Boot service then open a web browser and navigate to http://localhost:8083/api/ping

NOTE: That is port 8083, not the usual port 8080.

You should get back a response that says "pong".

## Try it Out
Let's show the benefits of using async in the outbound network calls from our service. First we'll show what
it's like with synchronous calls.

If you're running it as a stand-alone Spring-Boot service then access http://localhost:8083/api/sync/tenant/{name}

For example http://localhost:8083/api/sync/tenant/Customer1

This will store the string "Customer1" in Thread Local Storage, make a synchronous outbound call, then return with
whatever was store in Thread Local Storage.  That means that you should get the tenant's name, in this case `Customer1`.

It will take 2 minutes for that API call to complete. If you make the client API calls in parallel 20 times, then since we have a max
thread pool of 10 then it will take ~4 minutes total (20 calls / 10 threads in the pool * 2 minutes per invocation).

## Notes
When running it as a stand-alone SpringBoot the thread pool is set to 10 threads.  You can see that by calling the
'ping' API repeatedly.  You'll see something like this:

```
Handling request for 'ping' on thread http-nio-8083-exec-1
Handling request for 'ping' on thread http-nio-8083-exec-2
Handling request for 'ping' on thread http-nio-8083-exec-3
Handling request for 'ping' on thread http-nio-8083-exec-4
Handling request for 'ping' on thread http-nio-8083-exec-5
Handling request for 'ping' on thread http-nio-8083-exec-6
Handling request for 'ping' on thread http-nio-8083-exec-7
Handling request for 'ping' on thread http-nio-8083-exec-8
Handling request for 'ping' on thread http-nio-8083-exec-9
Handling request for 'ping' on thread http-nio-8083-exec-10
Handling request for 'ping' on thread http-nio-8083-exec-1
Handling request for 'ping' on thread http-nio-8083-exec-2
```

And so on.

If you run the war file in a Tomcat server then be sure to modify Tomcat's `conf\server.xml` so that it sets the 
`maxThreads` to 10.  For example in `server.xml`:

```xml
    <Executor name="tomcatThreadPool" namePrefix="catalina-exec-" maxThreads="10" minSpareThreads="10"/>
```


