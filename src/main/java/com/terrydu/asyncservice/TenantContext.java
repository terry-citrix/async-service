package com.terrydu.asyncservice;

import java.io.InputStream;
import java.io.OutputStream;

public class TenantContext {
    private String tenantName;
    private InputStream httpInputStream;
    private OutputStream httpOutputStream;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}
