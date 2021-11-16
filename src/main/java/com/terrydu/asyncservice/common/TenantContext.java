package com.terrydu.asyncservice.common;

public class TenantContext {

    public static ThreadLocal<String> tenantName = new ThreadLocal<>();

}
