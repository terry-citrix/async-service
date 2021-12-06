package com.terrydu.asyncservice.api.mvc.controller;

import static com.terrydu.asyncservice.api.Constant.SERVICE_URL_15;

import com.terrydu.asyncservice.api.HttpResponse;
import com.terrydu.asyncservice.api.HttpService;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
@RestController
public class AsyncTenantController {

  @Autowired
  private HttpService httpService;

  @GetMapping("/async/tenant/{tenantName}")
  @Produces({MediaType.APPLICATION_JSON})
  public Single<ResponseEntity<String>> getTenantByName(@PathVariable String tenantName) {
    System.out.println("Handling request for '/api/async/tenant/" + tenantName + "' on thread " + Thread.currentThread().getName());
    return something(tenantName);
  }

  public Single<ResponseEntity<String>> something(String tenantName) {
    ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
    threadLocalTenantName.set(tenantName);

    Single<HttpResponse> stringSingle = httpService.callHttp(tenantName, SERVICE_URL_15);
    return stringSingle
        .subscribeOn(Schedulers.io())
        .map(s -> ResponseEntity.ok().body("{\"response\":\"" + s.getResponse() + "\",\"tenant\":\"" + s.getThreadLocalTenantName() + "\"}"));

  }

}
