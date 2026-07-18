package com.nitax.valueplusbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@CrossOrigin(origins = "*")
public class ValuePlusBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(ValuePlusBackendApplication.class, args);
    int numOfCores = Runtime.getRuntime().availableProcessors();
    System.out.println("Number of cores present: " + numOfCores);
    System.out.println("Memory Info: " + getMemoryInfo());
  }

  public static String getMemoryInfo() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory(); // Maximum memory the JVM can use
    long totalMemory = runtime.totalMemory(); // Total memory currently available to the JVM
    long freeMemory = runtime.freeMemory(); // Free memory currently available to the JVM

    return String.format(
        "Max Memory: %d MB, Total Memory: %d MB, Free Memory: %d MB",
        maxMemory / (1024 * 1024), totalMemory / (1024 * 1024), freeMemory / (1024 * 1024));
  }
}
