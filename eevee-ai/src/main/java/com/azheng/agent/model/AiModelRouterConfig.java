/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azheng.agent.model;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型路由配置类
 * 负责创建熔断包装器和路由实例
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiModelRouterConfig {

    private final List<AiProvider> aiProviders;
    private final AiProviderProperties properties;

    /**
     * 创建熔断配置
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(100)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(10)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    /**
     * 创建重试配置
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofSeconds(1))
                .build();

        return RetryRegistry.of(config);
    }

    /**
     * 创建限流配置
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        return RateLimiterRegistry.of(config);
    }

    /**
     * 创建带熔断的 Provider 包装器列表（按优先级排序）
     */
    @Bean
    public List<AiProviderWrapper> providerWrappers(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            RateLimiterRegistry rateLimiterRegistry) {

        return aiProviders.stream()
                .map(provider -> {
                    String providerName = AiProviderWrapper.extractProviderName(provider);
                    String registryName = providerName.toLowerCase();

                    // 为每个 Provider 创建独立的熔断、重试、限流实例
                    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(registryName);
                    Retry retry = retryRegistry.retry(registryName);
                    RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(registryName);

                    return new AiProviderWrapper(provider, circuitBreaker, retry, rateLimiter);
                })
                // 按配置的优先级排序
                .sorted(Comparator.comparingInt(this::getProviderOrder))
                .collect(Collectors.toList());
    }

    /**
     * 获取 Provider 的优先级
     */
    private int getProviderOrder(AiProviderWrapper wrapper) {
        String name = wrapper.getProviderName().toLowerCase();
        var config = properties.getConfigByName(name);
        return config != null && config.getOrder() != null ? config.getOrder() : Integer.MAX_VALUE;
    }

    /**
     * 创建路由核心组件
     */
    @Bean
    public AiModelRouter aiModelRouter(List<AiProviderWrapper> providerWrappers) {
        log.info("初始化 AI 模型路由，Provider 数量: {}", providerWrappers.size());
        providerWrappers.forEach(w -> log.info("  - {} (优先级: {})",
                w.getProviderName(), getProviderOrder(w)));
        return new AiModelRouterImpl(providerWrappers, properties);
    }
}