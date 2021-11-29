package com.terrydu.asyncservice.api.servlet.controller;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TerryAsyncListener implements AsyncListener {

    String tenantName;
    ThreadLocal<String> threadLocalTenantName;
    long startTime;
    HttpServletResponse servletResponse;
    String response = "";

    public TerryAsyncListener(String tenantName,
                              ThreadLocal<String> threadLocalTenantName,
                              long startTime,
                              HttpServletResponse servletResponse) {
        this.tenantName = tenantName;
        this.threadLocalTenantName = threadLocalTenantName;
        this.startTime = startTime;
        this.servletResponse = servletResponse;
    }

    public void setResponse(String response) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Setting the response from the external call to our AsyncListener");
        this.response = response;
    }

    public void onComplete(AsyncEvent event) throws IOException {
        threadLocalTenantName.set(tenantName);

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/servlet/sync/tenant?name=" + tenantName + "' taking " + timeElapsed + " ms");

        event.getSuppliedResponse().getOutputStream().print(threadLocalTenantName.get() + "-" + response);
    }

    public void onError(AsyncEvent event) {
        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR: " + event.getThrowable());
    }

    public void onStartAsync(AsyncEvent event) {
    }

    public void onTimeout(AsyncEvent event) {
        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR: Timeout in asyncListener.onTimeout");
    }
}