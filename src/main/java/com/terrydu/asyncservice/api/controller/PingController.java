package com.terrydu.asyncservice.api.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/ping")
public class PingController {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String ping() {
        System.out.println("Handling request for 'ping' on thread " + Thread.currentThread().getName());
        return "pong";
    }
}
