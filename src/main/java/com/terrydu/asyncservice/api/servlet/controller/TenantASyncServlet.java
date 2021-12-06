package com.terrydu.asyncservice.api.servlet.controller;

import static com.terrydu.asyncservice.api.Constant.SERVICE_URL_15;

import com.terrydu.asyncservice.api.HttpService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The URL is a bit different, more web-friendly and not REST.  It would be: http://localhost:8083/api/servlet/async/tenant?name={tenantName} for example: http://localhost:8083/api/servlet/sync/tenant?name=Customer1
 */
@WebServlet(name = "TenantASyncServlet", urlPatterns = "/api/servlet/async/tenant", asyncSupported = true)
public class TenantASyncServlet extends HttpServlet {

  @Inject
  private HttpService httpService;

  @Override
  protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
    final AsyncContext asyncContext = servletRequest.startAsync(servletRequest, servletResponse);
    //Set the content type
    servletResponse.setContentType("application/json");

    //Get the output stream writer
    PrintWriter out = servletResponse.getWriter();

    String tenantName = servletRequest.getParameter("name");
    httpService.callJersey(tenantName, SERVICE_URL_15)
        .subscribeOn(Schedulers.io())
        .subscribe(
            s -> {
              out.write("{\"response\":\"" + s.getResponse() + "\",\"tenant\":\"" + s.getThreadLocalTenantName() + "\"}");
            },
            t -> {
              System.out.println(t.getMessage());
              out.write("{\"response\":\"" + t.getMessage() + "\",\"tenant\":\"ERROR\"}");
            },
            () -> {
              System.out.println("Closing the response.");
              asyncContext.complete();
            }

        );
  }
}
