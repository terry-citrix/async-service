package com.terrydu.asyncservice;

import com.terrydu.asyncservice.api.controller.AsyncTenantController;
import com.terrydu.asyncservice.api.controller.PingController;
import com.terrydu.asyncservice.api.controller.SyncTenantController;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(PingController.class);
        register(SyncTenantController.class);
        register(AsyncTenantController.class);
    }
}