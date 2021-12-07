package com.terrydu.asyncservice.api.servlet.controller;

import com.terrydu.asyncservice.TenantContext;
import com.terrydu.asyncservice.executors.OnResponseReceived;
import com.terrydu.asyncservice.executors.OnResponseReceivedExecutor;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 * The URL is a bit different, more web-friendly and not REST.  It would be:
 *    http://localhost:8083/api/servlet/async/tenant?name={tenantName}
 * for example:
 *    http://localhost:8083/api/servlet/async/tenant?name=Customer1
 */
@WebServlet(name = "TenantAsyncServlet", urlPatterns = "/api/servlet/async/tenant", asyncSupported = true)
public class TenantAsyncServlet extends HttpServlet {

    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";

    @Autowired
    private OnResponseReceivedExecutor onResponseReceivedExecutor;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    @Override
    protected void doGet(HttpServletRequest servletRequest,
                         HttpServletResponse servletResponse)
    {
        String tenantName = servletRequest.getParameter("name");
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/servlet/sync/tenant?name=" + tenantName + "'");
        servletRequest.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        AsyncContext asyncContext = servletRequest.startAsync();
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        ListenableFuture<Response> whenResponse = asyncHttpClient.prepareGet(SERVICE_URL_15).execute();
        TenantContext tenantContext = new TenantContext();
        tenantContext.setTenantName(tenantName);

        OnResponseReceived onResponseReceived = new OnResponseReceived(whenResponse, tenantContext, asyncContext, asyncHttpClient );
        whenResponse.addListener(onResponseReceived, onResponseReceivedExecutor.getOnResponseReceivedExecutor());
    }
}