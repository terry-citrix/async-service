package com.terrydu.asyncservice.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/ping")
public class PingController {

 //   @Autowired
 //   private PingController pingController;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String ping() {
        return "pong";
    }
}