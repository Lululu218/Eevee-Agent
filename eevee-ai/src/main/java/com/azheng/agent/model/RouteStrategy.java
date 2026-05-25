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

/**
 * 模型厂商路由策略枚举类
 */
public enum RouteStrategy {
    /**
     * 优先级策略：按配置的 order 优先级选择（默认）
     */
    PRIORITY,

    /**
     * 轮询策略：依次选择每个 Provider
     */
    ROUND_ROBIN,

    /**
     * 故障转移策略：主 Provider 故障时自动切换到备用
     */
    FAILOVER,

    /**
     * 负载均衡策略：根据响应时间和错误率动态选择
     */
    LOAD_BALANCE
}
