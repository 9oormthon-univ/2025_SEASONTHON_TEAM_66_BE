package com.goormthon.careroad.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("io-");
        ex.setCorePoolSize(8);    // I/O는 대기 비율이 높으므로 넉넉히
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(2000);
        ex.setKeepAliveSeconds(60);
        ex.setAllowCoreThreadTimeOut(true);
        ex.initialize();
        return ex;
    }

    @Bean(name = "cpuExecutor")
    public Executor cpuExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("cpu-");
        ex.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors()));
        ex.setMaxPoolSize(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));
        ex.setQueueCapacity(256);
        ex.setKeepAliveSeconds(30);
        ex.setAllowCoreThreadTimeOut(true);
        ex.initialize();
        return ex;
    }

    @Override
    public Executor getAsyncExecutor() {
        // @Async 기본 풀(미지정 시)
        return ioExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                System.err.println("[@Async] Uncaught in " + method + " : " + ex.getMessage());
    }

    @Bean("jobExecutor")
    public Executor jobExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("job-");
        ex.initialize();
        return ex;
    }
}
