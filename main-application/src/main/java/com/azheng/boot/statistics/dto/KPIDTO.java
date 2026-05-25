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
package com.azheng.boot.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * KPI指标数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KPIDTO {

    /**
     * 活跃用户数
     */
    private Long activeUsers;

    /**
     * 会话总数
     */
    private Long totalConversations;

    /**
     * 消息总数
     */
    private Long totalMessages;

    /**
     * 平均响应时间(ms)
     */
    private BigDecimal avgResponseTime;

    /**
     * 知识库命中率(%)
     */
    private BigDecimal kbHitRate;

    /**
     * AI调用成功率(%)
     */
    private BigDecimal successRate;

    /**
     * 会话深度（平均轮数）
     */
    private BigDecimal avgConversationDepth;

    /**
     * 今日新增用户数
     */
    private Long newUsers;

    /**
     * 活跃用户环比增长率(%)
     */
    private BigDecimal activeUsersGrowth;

    /**
     * 会话数环比增长率(%)
     */
    private BigDecimal conversationsGrowth;

    /**
     * 消息数环比增长率(%)
     */
    private BigDecimal messagesGrowth;

    /**
     * 响应时间环比变化率(%)
     */
    private BigDecimal responseTimeChange;
}
