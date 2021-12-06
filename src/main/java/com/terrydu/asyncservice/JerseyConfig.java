package com.terrydu.asyncservice;

import com.terrydu.asyncservice.api.jersey.controller.AsyncTenantController;
import com.terrydu.asyncservice.api.jersey.controller.PingController;
import com.terrydu.asyncservice.api.jersey.controller.SyncTenantController;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ApplicationPath("/api/jersey/")
public class JerseyConfig extends ResourceConfig {

  public JerseyConfig() {
    register(PingController.class);
    register(SyncTenantController.class);
    register(AsyncTenantController.class);
  }
}
