package com.terrydu.asyncservice.executors;

import com.terrydu.asyncservice.TenantContext;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.AsyncContext;
import javax.ws.rs.container.AsyncResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

public class OnResponseReceived implements Runnable {
    private final ListenableFuture<Response> whenResponse;
    private final String tenantName;
    private final AsyncResponse asyncResponse;
    private final DeferredResult<ResponseEntity<String>> deferredResult;
    private final AsyncContext asyncContext;
    private final AsyncHttpClient asyncClient;

    public OnResponseReceived(ListenableFuture<Response> whenResponse, TenantContext tenantContext, AsyncResponse asyncResponse, AsyncHttpClient asyncClient) {
        this.whenResponse = whenResponse;
        this.tenantName = tenantContext.getTenantName();
        this.asyncResponse = asyncResponse;
        this.deferredResult = null;
        this.asyncContext = null;
        this.asyncClient = asyncClient;
    }

    public OnResponseReceived(ListenableFuture<Response> whenResponse, TenantContext tenantContext, DeferredResult<ResponseEntity<String>> deferredResult, AsyncHttpClient asyncClient) {
        this.whenResponse = whenResponse;
        this.tenantName = tenantContext.getTenantName();
        this.deferredResult = deferredResult;
        this.asyncResponse = null;
        this.asyncContext = null;
        this.asyncClient = asyncClient;
    }

    public OnResponseReceived(ListenableFuture<Response> whenResponse, TenantContext tenantContext, AsyncContext asyncContext, AsyncHttpClient asyncClient) {
        this.whenResponse = whenResponse;
        this.tenantName = tenantContext.getTenantName();
        this.deferredResult = null;
        this.asyncResponse = null;
        this.asyncContext = asyncContext;
        this.asyncClient = asyncClient;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        try  {
            ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
            threadLocalTenantName.set(this.tenantName);

            Response response = whenResponse.get();
            String httpResponse = response.getResponseBody();
            System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/async/tenant/" + tenantName);
            if (!tenantName.equals(threadLocalTenantName.get())) {
                System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
            }

            if(asyncResponse != null) {
                asyncResponse.resume(javax.ws.rs.core.Response.ok(threadLocalTenantName.get() + "-" + httpResponse).build());
            } else if(deferredResult != null) {
                deferredResult.setResult(ResponseEntity.ok(threadLocalTenantName.get() + "-" + httpResponse));
            } else if (asyncContext != null){
                PrintWriter out = asyncContext.getResponse().getWriter();
                out.write(threadLocalTenantName.get() + "-" + httpResponse);
                asyncContext.complete();
                out.close();
            } else {
                System.out.println("ERROR: Unhandled implementation.");
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("ERROR: Exception occurred : " + e.getMessage());
        } finally {
            try {
                asyncClient.close();
            } catch (Exception ex) {
                System.out.println("ERROR: Can't properly close the AsyncClient. Details: " + ex.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Took " + (endTime-startTime) + "ms inside of the Runnable.");
    }
}
