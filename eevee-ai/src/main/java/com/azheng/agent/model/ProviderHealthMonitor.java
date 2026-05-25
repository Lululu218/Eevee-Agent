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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provider 健康监控组件
 * 定期检查所有 Provider 的健康状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderHealthMonitor {

    private final List<AiProviderWrapper> providerWrappers;

    /**
     * 健康状态缓存
     */
    private final Map<String, ProviderHealthStatus> healthStatusMap = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // 初始化所有 Provider 的健康状态
        providerWrappers.forEach(w -> {
            healthStatusMap.put(w.getProviderName(), ProviderHealthStatus.builder()
                    .providerName(w.getProviderName())
                    .healthy(true)
                    .circuitBreakerState(w.getCircuitBreaker().getState())
                    .build());
        });

        // 每30秒检查一次健康状态
        scheduler.scheduleAtFixedRate(this::checkAllProviders, 0, 30, TimeUnit.SECONDS);
        log.info("Provider 健康监控已启动");
    }

    /**
     * 检查所有 Provider 的健康状态
     */
    private void checkAllProviders() {
        providerWrappers.forEach(wrapper -> {
            String name = wrapper.getProviderName();
            var state = wrapper.getCircuitBreaker().getState();
            boolean isHealthy = state != io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;

            healthStatusMap.compute(name, (key, status) -> {
                if (status == null) {
                    status = new ProviderHealthStatus();
                    status.setProviderName(name);
                }

                status.setHealthy(isHealthy);
                status.setCircuitBreakerState(state);
                status.setErrorRate(wrapper.getCircuitBreaker().getMetrics().getFailureRate());

                //待完善
                status.setAvgResponseTime(wrapper.getCircuitBreaker().getMetrics().getNumberOfSuccessfulCalls());

                return status;
            });

            if (!isHealthy) {
                log.warn("Provider {} 状态异常: {}", name, state);
            }
        });
    }

    /**
     * 获取指定 Provider 的健康状态
     */
    public ProviderHealthStatus getHealthStatus(String providerName) {
        return healthStatusMap.getOrDefault(providerName,
                ProviderHealthStatus.builder().providerName(providerName).build());
    }

    /**
     * 获取所有健康的 Provider 名称
     */
    public List<String> getHealthyProviders() {
        return healthStatusMap.entrySet().stream()
                .filter(entry -> entry.getValue().isHealthy())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 获取所有 Provider 的健康状态
     */
    public Map<String, ProviderHealthStatus> getAllHealthStatus() {
        return Map.copyOf(healthStatusMap);
    }
}