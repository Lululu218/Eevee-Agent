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
import java.util.List;

/**
 * 趋势数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDTO {

    /**
     * 时间标签列表
     */
    private List<String> labels;

    /**
     * 会话数序列
     */
    private List<Long> sessionCounts;

    /**
     * 消息数序列
     */
    private List<Long> messageCounts;

    /**
     * 响应时间序列(ms)
     */
    private List<Double> responseTimes;

    /**
     * 分布数据项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributionItem {
        /**
         * 分类名称
         */
        private String name;

        /**
         * 数值
         */
        private Long value;

        /**
         * 占比(%)
         */
        private BigDecimal percentage;

        /**
         * 颜色（用于图表展示）
         */
        private String color;
    }
}
