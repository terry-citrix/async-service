package com.terrydu.asyncservice.executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class OnResponseReceivedExecutor {
    private final ThreadPoolTaskExecutor executor;

    @Autowired
    public OnResponseReceivedExecutor() {
        //TODO: to check if we can use existing tomcat thread pool executor
        //executorService = Executors.newFixedThreadPool(10);

        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("response-handler-thread-");
        executor.initialize();
    }

    public ThreadPoolTaskExecutor getOnResponseReceivedExecutor() {
        return executor;
    }
}
