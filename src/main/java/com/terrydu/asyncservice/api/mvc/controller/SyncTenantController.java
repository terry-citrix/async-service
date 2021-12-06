package com.terrydu.asyncservice.api.mvc.controller;

import static com.terrydu.asyncservice.api.Constant.SERVICE_URL_15;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * http://localhost:8083/api/mvc/sync/tenant/{tenantName}
 */
@RestController
public class SyncTenantController {

  /**
   * NOTE: This path mapping is relative to "/api/mvc"
   */
  @GetMapping("/sync/tenant/{tenantName}")
  public String syncTenant(@PathVariable String tenantName) {
    System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/mvc/sync/tenant/" + tenantName + "'");
    long startTime = System.currentTimeMillis();

    ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
    threadLocalTenantName.set(tenantName);

    // Call external API that takes 15 seconds here.
    final HttpUriRequest request = new HttpGet(SERVICE_URL_15);

    String response = "";
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      try (CloseableHttpResponse httpResponse = httpclient.execute(request)) {
        int statusCode = httpResponse.getCode();
        if (statusCode != HttpStatus.SC_OK) {
          System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR from the external network service call! Status returned: " + statusCode);
        } else {
          HttpEntity entity = httpResponse.getEntity();
          try {
            response = EntityUtils.toString(entity);
          } catch (ParseException ex) {
            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Error parsing response after calling " + SERVICE_URL_15);
          }

        }
      }
    } catch (IOException ex) {
      System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR waiting for the external network service call!");
    }

    if (!tenantName.equals(threadLocalTenantName.get())) {
      System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
    }

    long endTime = System.currentTimeMillis();
    long timeElapsed = endTime - startTime;
    System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/mvc/sync/tenant/" + tenantName + "' taking " + timeElapsed + " ms");
    return threadLocalTenantName.get() + "-" + response;
  }
}
