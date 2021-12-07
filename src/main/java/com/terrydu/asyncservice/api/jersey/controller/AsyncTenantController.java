package com.terrydu.asyncservice.api.jersey.controller;


import com.terrydu.asyncservice.TenantContext;
import com.terrydu.asyncservice.executors.OnResponseReceived;
import com.terrydu.asyncservice.executors.OnResponseReceivedExecutor;
import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import static org.asynchttpclient.Dsl.*;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
// This is relative to http://hostname/api/jersey
@Path("/async/tenant")
public class AsyncTenantController {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";
    private final OnResponseReceivedExecutor onResponseReceivedExecutor;

    @Autowired
    public AsyncTenantController(OnResponseReceivedExecutor onResponseReceivedExecutor){
        this.onResponseReceivedExecutor = onResponseReceivedExecutor;
    }

    @GET
    @Path("/{tenantName}")
    public void getTenantByName(@PathParam("tenantName") String tenantName, @Suspended final AsyncResponse inAsyncResponse) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/jersey/async/tenant/" + tenantName + "'");
        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        ListenableFuture<Response> whenResponse = asyncHttpClient.prepareGet(SERVICE_URL_15).execute();
        TenantContext tenantContext = new TenantContext();
        tenantContext.setTenantName(tenantName);

        OnResponseReceived onResponseReceived = new OnResponseReceived(whenResponse, tenantContext, inAsyncResponse, asyncHttpClient);
        whenResponse.addListener(onResponseReceived, onResponseReceivedExecutor.getOnResponseReceivedExecutor());
    }
}
