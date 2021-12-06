package com.terrydu.asyncservice.api.jersey.controller;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
// This is relative to http://hostname/api/jersey
@Path("sync/tenant")
public class SyncTenantController {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";
    private static final Logger logger = LoggerFactory.getLogger(SyncTenantController.class);

    @GET
    @Path("/{tenantName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTenantByName(@PathParam("tenantName") String tenantName) {
        logger.info("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/jersey/sync/tenant/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Call external API that takes 2 minutes here.
        final HttpUriRequest request = new HttpGet(SERVICE_URL_15);

        String response = "";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse httpResponse = httpclient.execute(request)) {
                int statusCode = httpResponse.getCode();
                if (statusCode != HttpStatus.SC_OK) {
                    logger.error("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR from the external network service call! Status returned: " + statusCode);
                } else {
                    HttpEntity entity = httpResponse.getEntity();
                    try {
                        response = EntityUtils.toString(entity);
                    } catch (ParseException ex) {
                        logger.error("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Error parsing response after calling " + SERVICE_URL_15);
                    }

                }
            }
        } catch (IOException ex) {
            logger.error("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR waiting for the external network service call!");
        }

        if (!tenantName.equals(threadLocalTenantName.get())) {
            logger.error("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        logger.info("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/jersey/sync/tenant/" + tenantName + "' taking " + timeElapsed + " ms");
        return threadLocalTenantName.get() + "-" + response;
    }

}
