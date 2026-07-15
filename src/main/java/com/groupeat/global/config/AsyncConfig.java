package com.groupeat.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String NOTIFICATION_TASK_EXECUTOR = "notificationTaskExecutor";

    @Bean(name = NOTIFICATION_TASK_EXECUTOR)
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2); // 기본 스레드 수
        executor.setMaxPoolSize(5); // 최대 스레드 수
        executor.setQueueCapacity(100); // 큐 대기 작업 개수
        executor.setThreadNamePrefix("notification-"); // 스레드 이름 지정

        executor.initialize();
        return executor;
    }
}
