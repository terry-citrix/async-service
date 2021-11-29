package com.terrydu.asyncservice.api.servlet.controller;

import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The URL is a bit different, more web-friendly and not REST.  It would be:
 *    http://localhost:8083/api/servlet/sync/tenant?name={tenantName}
 * for example:
 *    http://localhost:8083/api/servlet/sync/tenant?name=Customer1
 */
@WebServlet(name = "TerryAsyncServlet", urlPatterns = "/api/servlet/terry/tenant", asyncSupported=true)
public class TerryAsyncServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException
    {
        String tenantName = servletRequest.getParameter("name");

        AsyncContext context = servletRequest.startAsync();

        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/servlet/sync/tenant?name=" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Set up the async listener.
        context.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent event) throws IOException {
                event.getSuppliedResponse().getOutputStream().print("Complete");

            }

            public void onError(AsyncEvent event) {
                System.err.println("ERROR: " + event.getThrowable());
            }

            public void onStartAsync(AsyncEvent event) {
            }

            public void onTimeout(AsyncEvent event) {
                System.err.println("ERROR: Timeout in asyncListener.onTimeout");
            }
        });
        ServletInputStream input = servletRequest.getInputStream();
        ReadListener readListener = new TerryReadListener(input, servletResponse, context);
        input.setReadListener(readListener);
    }

}
