package com.terrydu.asyncservice.api.jersey.controller;

import static com.terrydu.asyncservice.api.Constant.SERVICE_URL_15;

import com.terrydu.asyncservice.api.HttpResponse;
import com.terrydu.asyncservice.api.HttpService;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
// This is relative to http://hostname/api/jersey
@Path("/async/tenant")
public class AsyncTenantController {

  @Autowired
  public HttpService httpService;

  @GET
  @Path("/{tenantName}")
  @Produces({MediaType.APPLICATION_JSON})
  public void getTenantByName(@PathParam("tenantName") String tenantName, @Suspended final AsyncResponse async) {
    System.out.println("Calling Terry URL, tenant: " + tenantName + "' on thread " + Thread.currentThread().getName());

    Observable<HttpResponse> observable = httpService.callJersey(tenantName, SERVICE_URL_15);
    observable.subscribeOn(Schedulers.io()).subscribe(async::resume, async::resume);
  }
}
