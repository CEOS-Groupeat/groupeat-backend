package com.groupeat.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

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
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 큐 포화 시 요청 스레드에서 직접 실행
        executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 대기 중인 작업 완료까지 대기
        executor.setAwaitTerminationSeconds(30); // 종료 대기 최대 시간

        executor.initialize();
        return executor;
    }
}
