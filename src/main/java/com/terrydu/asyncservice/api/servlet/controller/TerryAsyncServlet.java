package com.terrydu.asyncservice.api.servlet.controller;

import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.concurrent.Future;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The URL is a bit different, more web-friendly and not REST.  It would be:
 *    http://localhost:8083/api/servlet/terry/tenant?name={tenantName}
 * for example:
 *    http://localhost:8083/api/servlet/terry/tenant?name=Customer1
 */
@WebServlet(name = "TerryAsyncServlet", urlPatterns = "/api/servlet/terry/tenant", asyncSupported=true)
public class TerryAsyncServlet extends HttpServlet {

    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";

    @Override
    protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException
    {
        String tenantName = servletRequest.getParameter("name");
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/servlet/sync/tenant?name=" + tenantName + "'");
        long startTime = System.currentTimeMillis();


        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Set up the async listener.
        AsyncContext context = servletRequest.startAsync();
        TerryAsyncListener terryAsyncListener = new TerryAsyncListener(tenantName, threadLocalTenantName, startTime, servletResponse);
        context.addListener(terryAsyncListener);

        //
        // Call external network API that takes a long time here.
        //
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(180))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .build();

        client.start();

        final HttpHost target = new HttpHost("terrydu-wait.azurewebsites.net");
        final SimpleHttpRequest simpleRequest = SimpleRequestBuilder.get()
                .setHttpHost(target)
                .setPath("/api/terrydu-wait15")
                .build();

        client.execute(
                SimpleRequestProducer.create(simpleRequest),
                SimpleResponseConsumer.create(),
                new FutureCallback<SimpleHttpResponse>() {

                    @Override
                    public void completed(final SimpleHttpResponse httpResponse) {
                        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": " + simpleRequest + "->" + httpResponse.getCode());

                        SimpleBody httpBody = httpResponse.getBody();
                        terryAsyncListener.setResponse(httpBody.getBodyText());     // Write the response to our AsyncListener
                        client.close(CloseMode.GRACEFUL);

                        context.complete();         // This results in onComplete(AsyncEvent) getting called.
                    }

                    @Override
                    public void failed(final Exception ex) {
                        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": " + simpleRequest + "->" + ex);
                        client.close(CloseMode.GRACEFUL);
                    }

                    @Override
                    public void cancelled() {
                        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR for " + simpleRequest + " - cancelled");
                        client.close(CloseMode.GRACEFUL);
                    }

                });
    }

}
