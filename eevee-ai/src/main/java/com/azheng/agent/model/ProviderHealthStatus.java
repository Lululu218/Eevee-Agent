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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型厂商健康状态类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderHealthStatus {
    /**
     * Provider 名称
     */
    private String providerName;

    /**
     * 是否健康
     */
    private boolean healthy;

    /**
     * 当前熔断状态
     */
    private CircuitBreaker.State circuitBreakerState;

    /**
     * 错误率
     */
    private double errorRate;

    /**
     * 平均响应时间(ms)
     */
    private long avgResponseTime;

    /**
     * 连续失败次数
     */
    private int consecutiveFailures;
}
