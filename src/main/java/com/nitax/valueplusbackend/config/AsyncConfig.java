package com.nitax.valueplusbackend.config;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig extends ThreadPoolTaskExecutor implements AsyncConfigurer {
  ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {

    executor.setCorePoolSize(100);
    executor.setMaxPoolSize(250);
    executor.setQueueCapacity(5000);
    executor.setThreadNamePrefix("conversion-thread - ");
    executor.initialize();
    return executor;
  }

  @Override
  public Executor getAsyncExecutor() {
    return executor;
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    log.info("Thread {} has completed task {}", Thread.currentThread().getName(), r);
  }

  @PreDestroy
  public void destroy() {
    log.info("Shutting down the async task executor");
    this.executor.shutdown();
  }
}
