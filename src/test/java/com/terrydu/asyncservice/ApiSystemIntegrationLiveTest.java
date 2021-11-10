package com.terrydu.asyncservice;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiSystemIntegrationLiveTest {

    private static final String SERVICE_URL = "http://localhost:8083/api/ping";

    @Disabled("Only run this when the stand-alone service is running!")
    @Test
    public void getPing() throws IOException {
        final HttpUriRequest request = new HttpGet(SERVICE_URL);

        final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }
}