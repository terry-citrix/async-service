package com.terrydu.asyncservice.api.mvc.controller;

import com.terrydu.asyncservice.TenantContext;
import com.terrydu.asyncservice.executors.OnResponseReceived;
import com.terrydu.asyncservice.executors.OnResponseReceivedExecutor;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 *    http://localhost:8083/api/mvc/async/tenant/{tenantName}
 */
@RestController
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

    /**
     * NOTE: This path mapping is relative to "/api/mvc"
     */
    @GetMapping("/async/tenant/{tenantName}")
    public DeferredResult<ResponseEntity<String>> asyncTenant(@PathVariable String tenantName) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/mvc/async/tenant/" + tenantName + "'");
        DeferredResult<ResponseEntity<String>> asyncResponse = new DeferredResult<>();
        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        ListenableFuture<Response> whenResponse = asyncHttpClient.prepareGet(SERVICE_URL_15).execute();
        TenantContext tenantContext = new TenantContext();
        tenantContext.setTenantName(tenantName);

        OnResponseReceived onResponseReceived = new OnResponseReceived(whenResponse, tenantContext, asyncResponse, asyncHttpClient);
        whenResponse.addListener(onResponseReceived, onResponseReceivedExecutor.getOnResponseReceivedExecutor());
        return asyncResponse;
    }
}

