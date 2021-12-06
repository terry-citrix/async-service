package com.terrydu.asyncservice.executors;

import com.terrydu.asyncservice.TenantContext;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.AsyncContext;
import javax.ws.rs.container.AsyncResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

public class OnResponseReceived implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(OnResponseReceived.class);
    private final ListenableFuture<Response> whenResponse;
    private final long startTime;
    private final String tenantName;
    private final AsyncResponse asyncResponse;
    private final DeferredResult<ResponseEntity<String>> deferredResult;
    private final AsyncContext asyncContext;

    public OnResponseReceived(ListenableFuture<Response> whenResponse, long startTime, TenantContext tenantContext, AsyncResponse asyncResponse) {
        this.whenResponse = whenResponse;
        this.startTime = startTime;
        this.tenantName = tenantContext.getTenantName();
        this.asyncResponse = asyncResponse;
        this.deferredResult = null;
        this.asyncContext = null;
    }

    public OnResponseReceived(ListenableFuture<Response> whenResponse, long startTime, TenantContext tenantContext, DeferredResult<ResponseEntity<String>> deferredResult) {
        this.whenResponse = whenResponse;
        this.startTime = startTime;
        this.tenantName = tenantContext.getTenantName();
        this.deferredResult = deferredResult;
        this.asyncResponse = null;
        this.asyncContext = null;
    }

    public OnResponseReceived(ListenableFuture<Response> whenResponse, long startTime, TenantContext tenantContext, AsyncContext asyncContext) {
        this.whenResponse = whenResponse;
        this.startTime = startTime;
        this.tenantName = tenantContext.getTenantName();
        this.deferredResult = null;
        this.asyncResponse = null;
        this.asyncContext = asyncContext;
    }

    @Override
    public void run() {
        try  {
            ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
            threadLocalTenantName.set(this.tenantName);
            Response response = whenResponse.get();
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;
            String httpResponse = response.getResponseBody();
            logger.info("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/async/tenant/" + tenantName + "' taking " + timeElapsed + " ms");
            if (!tenantName.equals(threadLocalTenantName.get())) {
                logger.error("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
            }
            if(asyncResponse != null) {
                asyncResponse.resume(javax.ws.rs.core.Response.ok(threadLocalTenantName.get() + "-" + httpResponse).build());
            } else if(deferredResult != null) {
                deferredResult.setResult(ResponseEntity.ok(threadLocalTenantName.get() + "-" + httpResponse));
            } else if (asyncContext != null){
                PrintWriter out = asyncContext.getResponse().getWriter();
                out.write(threadLocalTenantName.get() + "-" + httpResponse);
                asyncContext.complete();
            } else {
                logger.error("Unhandled implementation.");
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Exception occurred : ", e);
        }
    }
}
