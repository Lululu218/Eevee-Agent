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

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/*
 * AI Provider 熔断包装器
 * 装饰器模式应用：扩展 模型熔断、重试、限流保护
 * 限流暂时先用框架实现，后续可以慢慢探索方案
 *
 * 框架运用：
 * circuitBreaker：熔断器
 * retry：自动重试失败请求
 * rateLimiter：控制请求频率
 * */
@Slf4j
@Getter
public class AiProviderWrapper {




    private final AiProvider provider;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final String providerName;

    /**
     * 从 Provider 实例提取名称
     */
    public static String extractProviderName(AiProvider provider) {
        String className = provider.getClass().getSimpleName();
        return className.replace("Provider", "");
    }

    public AiProviderWrapper(AiProvider provider,
                             CircuitBreaker circuitBreaker,
                             Retry retry,
                             RateLimiter rateLimiter) {
        this.provider = provider;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.rateLimiter = rateLimiter;
        this.providerName = extractProviderName(provider);
    }

    /**
     * 获取熔断后的流式聊天模型
     */
    public StreamingChatModel getStreamingChatModelWithProtection(Boolean isThinking) {
        return circuitBreaker.executeSupplier(() ->
                retry.executeSupplier(() ->
                        rateLimiter.executeSupplier(() -> {
                            log.debug("[AI-MODEL-WRAPPER] 获取流式模型: {}, thinking: {}", providerName, isThinking);
                            StreamingChatModel model = provider.getStreamingChatModel(isThinking);
                            log.debug("[AI-MODEL-WRAPPER] 流式模型获取成功: {}", providerName);
                            return model;
                        })
                )
        );
    }

    /**
     * 获取熔断后的后台任务模型
     */
    public ChatModel getBackgroundModelWithProtection() {
        return circuitBreaker.executeSupplier(() ->
                retry.executeSupplier(() ->
                        rateLimiter.executeSupplier(() -> {
                            log.debug("[AI-MODEL-WRAPPER] 获取后台模型: {}", providerName);
                            ChatModel model = provider.getBackgroundModel();
                            log.debug("[AI-MODEL-WRAPPER] 后台模型获取成功: {}", providerName);
                            return model;
                        })
                )
        );
    }

    /**
     * 获取熔断后的向量化模型
     */
    public EmbeddingModel getEmbeddingModelWithProtection() {
        return circuitBreaker.executeSupplier(() ->
                retry.executeSupplier(() ->
                        rateLimiter.executeSupplier(() -> {
                            log.debug("[AI-MODEL-WRAPPER] 获取向量化模型: {}", providerName);
                            EmbeddingModel model = provider.getEmbeddingModel();
                            log.debug("[AI-MODEL-WRAPPER] 向量化模型获取成功: {}", providerName);
                            return model;
                        })
                )
        );
    }

    /**
     * 检查 Provider 是否可用（熔断状态 + 配置启用）
     */
    public boolean isAvailable() {
        return circuitBreaker.getState() != CircuitBreaker.State.OPEN;
    }
}