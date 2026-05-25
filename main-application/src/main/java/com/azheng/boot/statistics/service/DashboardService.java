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
package com.azheng.boot.statistics.service;

import com.azheng.boot.statistics.dto.KPIDTO;
import com.azheng.boot.statistics.dto.TrendDTO;
import com.azheng.boot.statistics.dto.HotQuestionDTO;
import com.azheng.boot.statistics.dto.RealtimeEventDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 数据大屏服务接口
 */
public interface DashboardService {

    /**
     * 获取核心KPI指标
     *
     * @param timeRange 时间范围
     * @return KPI数据
     */
    KPIDTO getKPI(String timeRange);

    /**
     * 获取趋势数据
     *
     * @param timeRange 时间范围
     * @param type      数据类型
     * @return 趋势数据
     */
    TrendDTO getTrend(String timeRange, String type);

    /**
     * 创建实时事件流发射器
     *
     * @return SSE发射器
     */
    SseEmitter createRealtimeEmitter();

    /**
     * 获取热点问题排行
     *
     * @param limit 返回数量
     * @return 热点问题列表
     */
    List<HotQuestionDTO> getHotQuestions(int limit);

    /**
     * 获取实时会话列表
     *
     * @param limit 返回数量
     * @return 实时会话列表
     */
    List<RealtimeEventDTO> getRealtimeSessions(int limit);

    /**
     * 获取消息来源分布
     *
     * @param timeRange 时间范围
     * @return 分布数据
     */
    List<TrendDTO.DistributionItem> getMessageDistribution(String timeRange);

    /**
     * 推送实时事件
     *
     * @param event 事件数据
     */
    void pushRealtimeEvent(RealtimeEventDTO event);
}
