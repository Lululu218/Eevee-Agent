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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 多模型容错配置信息类
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {
    /**
     * 默认路由策略
     */
    private RouteStrategy defaultStrategy = RouteStrategy.PRIORITY;

    /**
     * 是否启用熔断
     */
    private boolean circuitBreakerEnabled = true;

    /**
     * 是否启用重试
     */
    private boolean retryEnabled = true;

    /**
     * 是否启用限流
     */
    private boolean rateLimiterEnabled = true;

    /**
     * 三种厂商
     */
    private ProviderConfig SiliconFlow = new ProviderConfig();
    private ProviderConfig BaiLian = new ProviderConfig();
    private ProviderConfig ZhiPu = new ProviderConfig();

    @Data
    public static class ProviderConfig {
        /**
         * 优先级（数字越小越优先）
         */
        private Integer order = Integer.MAX_VALUE;

        /**
         * 是否启用
         */
        private Boolean enabled = true;

        private String apiKey;
        private String baseUrl;
        private String streamModel;
        private String backgroundModel;
        private String embeddingModel;
        /**
         * 限流阈值（每秒请求数）
         */
        private Integer rateLimit = 100;
    }

    /**
     * 根据 Provider 名称获取配置
     */
    public ProviderConfig getConfigByName(String providerName) {
        return switch (providerName.toLowerCase()) {
            case "SiliconFlow", "siliconflow" -> SiliconFlow;
            case "BaiLian", "bailian" -> BaiLian;
            case "ZhiPu", "zhipu" -> ZhiPu;
            default -> null;
        };
    }
}
