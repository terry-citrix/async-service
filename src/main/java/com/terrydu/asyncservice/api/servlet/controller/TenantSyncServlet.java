package com.terrydu.asyncservice.api.servlet.controller;

import static com.terrydu.asyncservice.api.Constant.SERVICE_URL_15;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * The URL is a bit different, more web-friendly and not REST.  It would be: http://localhost:8083/api/servlet/sync/tenant?name={tenantName} for example: http://localhost:8083/api/servlet/sync/tenant?name=Customer1
 */
@WebServlet(name = "TenantSyncServlet", urlPatterns = "/api/servlet/sync/tenant")
public class TenantSyncServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    String tenantName = servletRequest.getParameter("name");

    try {

      System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/servlet/sync/tenant?name=" + tenantName + "'");
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
        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR waiting for the external network service call! Details: " + ex.getMessage());
      }

      if (!tenantName.equals(threadLocalTenantName.get())) {
        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
      }

      long endTime = System.currentTimeMillis();
      long timeElapsed = endTime - startTime;
      System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/servlet/sync/tenant?name=" + tenantName + "' taking " + timeElapsed + " ms");

      ServletOutputStream out = servletResponse.getOutputStream();
      servletResponse.setStatus(HttpServletResponse.SC_OK);
      servletResponse.setContentType("application/json");
      servletResponse.setHeader("Cache-Control", "no-cache");

      out.println(threadLocalTenantName.get() + "-" + response);
      out.close();

    } catch (Exception e) {
      System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR waiting for the external network service call!");
      servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
