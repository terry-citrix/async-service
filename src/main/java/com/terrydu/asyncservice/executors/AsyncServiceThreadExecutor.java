package com.terrydu.asyncservice.executors;

import org.apache.catalina.core.StandardThreadExecutor;

public class AsyncServiceThreadExecutor extends StandardThreadExecutor {
    public AsyncServiceThreadExecutor() {
        super();
        this.maxThreads = 10;
        this.namePrefix = "nitin-thread-";
    }
}
