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

import com.azheng.framework.exception.ServiceException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.CircuitBreakingException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AI 模型路由实现类
 * 支持多种路由策略和故障转移
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelRouterImpl implements AiModelRouter{

    private final List<AiProviderWrapper> providerWrappers;
    private final AiProviderProperties properties;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private volatile RouteStrategy currentStrategy;

    // 健康状态缓存
    private final Map<String, ProviderHealthStatus> healthStatusMap = new ConcurrentHashMap<>();


    @Override
    public StreamingChatModel routerStreamingCHatModel(Boolean isThinking) {
        AiProviderWrapper wrapper = selectProvider();
        return executeWithFailover(wrapper, () -> wrapper.getStreamingChatModelWithProtection(isThinking));
    }

    @Override
    public ChatModel routerChatModel() {
        AiProviderWrapper wrapper = selectProvider();
        return executeWithFailover(wrapper, wrapper::getBackgroundModelWithProtection);
    }

    @Override
    public EmbeddingModel routerEmbeddingModel() {
        AiProviderWrapper wrapper = selectProvider();
        return executeWithFailover(wrapper, wrapper::getEmbeddingModelWithProtection);
    }

    @Override
    public void switchStrategy(RouteStrategy strategy) {
        log.info("[AI-MODEL-ROUTER] 切换路由策略: {} -> {}", 
                currentStrategy != null ? currentStrategy.name() : "DEFAULT", strategy.name());
        this.currentStrategy = strategy;
    }
    /**
     * 根据当前策略选择 Provider
     */
    private AiProviderWrapper selectProvider() {
        // 1.获取当前路由策略
        RouteStrategy strategy = currentStrategy != null ? currentStrategy : properties.getDefaultStrategy();
        
        // 2.记录路由策略选择
        log.info("[AI-MODEL-ROUTER] 当前路由策略: {}, 可用Provider数量: {}", 
                strategy.name(), getAvailableProviderCount());

        // 3.根据对应策略选择出增强的模型厂商
        AiProviderWrapper selected = switch (strategy) {
            case PRIORITY -> selectByPriority();
            case ROUND_ROBIN -> selectByRoundRobin();
            case FAILOVER -> selectByFailover();
            case LOAD_BALANCE -> selectByLoadBalance();
        };
        
        // 4.记录选择结果
        log.info("[AI-MODEL-ROUTER] 策略[{}]选择Provider: {}, 熔断状态: {}", 
                strategy.name(), selected.getProviderName(), 
                selected.getCircuitBreaker().getState());
        
        return selected;
    }
    
    /**
     * 获取可用Provider数量
     */
    private int getAvailableProviderCount() {
        return (int) providerWrappers.stream()
                .filter(w -> {
                    String name = w.getProviderName().toLowerCase();
                    var config = properties.getConfigByName(name);
                    return config != null && Boolean.TRUE.equals(config.getEnabled());
                })
                .count();
    }

    /**
     * 优先级策略：选择优先级最高且可用的 Provider
     */
    private AiProviderWrapper selectByPriority() {
        return providerWrappers.stream()
                .filter(wrapper -> {
                    String name = wrapper.getProviderName().toLowerCase();
                    var config = properties.getConfigByName(name);
                    return config != null && Boolean.TRUE.equals(config.getEnabled());
                })
                .filter(AiProviderWrapper::isAvailable)
                .findFirst()
                .orElseThrow(() -> new ServiceException("没有可用的 AI Provider"));
    }

    /**
     * 轮询策略：依次选择每个 Provider
     */
    private AiProviderWrapper selectByRoundRobin() {
        List<AiProviderWrapper> available = providerWrappers.stream()
                .filter(w -> {
                    String name = w.getProviderName().toLowerCase();
                    var config = properties.getConfigByName(name);
                    return config != null && Boolean.TRUE.equals(config.getEnabled()) && w.isAvailable();
                })
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            throw new ServiceException("没有可用的 AI Provider");
        }

        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % available.size());
        return available.get(index);
    }

    /**
     * 故障转移策略：选择第一个可用的 Provider
     */
    private AiProviderWrapper selectByFailover() {
        return providerWrappers.stream()
                .filter(w -> {
                    String name = w.getProviderName().toLowerCase();
                    var config = properties.getConfigByName(name);
                    return config != null && Boolean.TRUE.equals(config.getEnabled());
                })
                .filter(AiProviderWrapper::isAvailable)
                .findFirst()
                .orElseThrow(() -> new ServiceException("没有可用的 AI Provider"));
    }

    /**
     * 负载均衡策略：选择响应时间最优的 Provider
     */
    private AiProviderWrapper selectByLoadBalance() {
        return providerWrappers.stream()
                .filter(w -> {
                    String name = w.getProviderName().toLowerCase();
                    var config = properties.getConfigByName(name);
                    return config != null && Boolean.TRUE.equals(config.getEnabled()) && w.isAvailable();
                })
                .min((a, b) -> {
                    long timeA = healthStatusMap.getOrDefault(a.getProviderName(),
                            ProviderHealthStatus.builder().avgResponseTime(Long.MAX_VALUE).build()).getAvgResponseTime();
                    long timeB = healthStatusMap.getOrDefault(b.getProviderName(),
                            ProviderHealthStatus.builder().avgResponseTime(Long.MAX_VALUE).build()).getAvgResponseTime();
                    return Long.compare(timeA, timeB);
                })
                .orElseThrow(() -> new RuntimeException("没有可用的 AI Provider"));
    }

    /**
     * 执行并处理故障转移
     * 捕获执行时出现的异常，然后按策略切换下一个厂商
     */
    private <T> T executeWithFailover(AiProviderWrapper primary, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            T result = supplier.get();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AI-MODEL-EXEC] Provider[{}] 执行成功, 耗时: {}ms, 熔断状态: {}", 
                    primary.getProviderName(), duration, 
                    primary.getCircuitBreaker().getState());
            updateHealthStatus(primary.getProviderName(), true, null, duration);
            return result;
        } catch (CircuitBreakingException e) {
            log.warn("[AI-MODEL-EXEC] Provider[{}] 熔断触发(OPEN状态), 开始故障转移", 
                    primary.getProviderName());
            return tryFailover(primary, supplier);
        } catch (RequestNotPermitted e) {
            log.warn("[AI-MODEL-EXEC] Provider[{}] 限流触发, 已达速率上限, 开始故障转移", 
                    primary.getProviderName());
            return tryFailover(primary, supplier);
        } catch (Exception e) {
            log.error("[AI-MODEL-EXEC] Provider[{}] 执行失败: {}, 异常类型: {}", 
                    primary.getProviderName(), e.getMessage(), 
                    e.getClass().getSimpleName());
            updateHealthStatus(primary.getProviderName(), false, e, 0);
            return tryFailover(primary, supplier);
        }
    }

    /**
     * 尝试故障转移到备用 Provider
     */
    private <T> T tryFailover(AiProviderWrapper failed, Supplier<T> supplier) {
        //1.获取所有启用的厂商
        List<AiProviderWrapper> backups = providerWrappers.stream()
                .filter(w -> !w.getProviderName().equals(failed.getProviderName()))
                .filter(w -> {
                    String name = w.getProviderName().toLowerCase();
                    var config = properties.getConfigByName(name);
                    return config != null && Boolean.TRUE.equals(config.getEnabled()) && w.isAvailable();
                })
                .collect(Collectors.toList());

        log.warn("[AI-MODEL-FAILOVER] 主Provider[{}]失败, 可用备用Provider数量: {}", 
                failed.getProviderName(), backups.size());

        //2.遍历处理
        for (int i = 0; i < backups.size(); i++) {
            AiProviderWrapper backup = backups.get(i);
            try {
                log.info("[AI-MODEL-FAILOVER] 尝试第{}/{}个备用Provider: {}", 
                        i + 1, backups.size(), backup.getProviderName());
                long startTime = System.currentTimeMillis();
                T result = switch (supplier.getClass().getSimpleName()) {
                    case "StreamingChatModelSupplier" -> (T) backup.getStreamingChatModelWithProtection(true);
                    case "BackgroundModelSupplier" -> (T) backup.getBackgroundModelWithProtection();
                    case "EmbeddingModelSupplier" -> (T) backup.getEmbeddingModelWithProtection();
                    default -> (T) backup.getBackgroundModelWithProtection();
                };
                long duration = System.currentTimeMillis() - startTime;
                log.info("[AI-MODEL-FAILOVER] 故障转移成功! Provider: {}, 耗时: {}ms", 
                        backup.getProviderName(), duration);
                updateHealthStatus(backup.getProviderName(), true, null, duration);
                return result;
            } catch (Exception e) {
                log.warn("[AI-MODEL-FAILOVER] 备用Provider[{}]失败: {}, 异常类型: {}", 
                        backup.getProviderName(), e.getMessage(), e.getClass().getSimpleName());
                updateHealthStatus(backup.getProviderName(), false, e, 0);
            }
        }

        log.error("[AI-MODEL-FAILOVER] 所有Provider均不可用! 主Provider: {}, 备用数量: {}", 
                failed.getProviderName(), backups.size());
        throw new RuntimeException("所有 Provider 均不可用");
    }

    /**
     * 更新健康状态
     */
    private void updateHealthStatus(String providerName, boolean success, Exception error, long responseTime) {
        healthStatusMap.compute(providerName, (key, status) -> {
            if (status == null) {
                status = new ProviderHealthStatus();
                status.setProviderName(providerName);
                status.setAvgResponseTime(responseTime);
            }

            if (success) {
                // 计算平均响应时间（简单滑动窗口）
                long newAvg = (status.getAvgResponseTime() + responseTime) / 2;
                status.setAvgResponseTime(newAvg);
                status.setHealthy(true);
                status.setConsecutiveFailures(0);
                status.setCircuitBreakerState(getCircuitBreakerState(providerName));
                
                // 健康状态恢复日志
                if (!status.isHealthy()) {
                    log.info("[AI-MODEL-HEALTH] Provider[{}] 健康状态恢复, 熔断状态: {}, 平均响应时间: {}ms", 
                            providerName, status.getCircuitBreakerState(), newAvg);
                }
            } else {
                status.setHealthy(false);
                int newConsecutiveFailures = status.getConsecutiveFailures() + 1;
                status.setConsecutiveFailures(newConsecutiveFailures);
                status.setCircuitBreakerState(getCircuitBreakerState(providerName));
                
                // 连续失败警告日志
                if (newConsecutiveFailures % 5 == 0) {
                    log.warn("[AI-MODEL-HEALTH] Provider[{}] 连续失败{}次, 熔断状态: {}", 
                            providerName, newConsecutiveFailures, status.getCircuitBreakerState());
                }
            }

            return status;
        });
    }

    private CircuitBreaker.State getCircuitBreakerState(String providerName) {
        return providerWrappers.stream()
                .filter(w -> w.getProviderName().equals(providerName))
                .map(AiProviderWrapper::getCircuitBreaker)
                .map(io.github.resilience4j.circuitbreaker.CircuitBreaker::getState)
                .findFirst()
                .orElse(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED);
    }
}
