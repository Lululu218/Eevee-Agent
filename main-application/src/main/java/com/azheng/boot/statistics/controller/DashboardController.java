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
package com.azheng.boot.statistics.controller;

import com.azheng.boot.statistics.dto.KPIDTO;
import com.azheng.boot.statistics.dto.TrendDTO;
import com.azheng.boot.statistics.dto.HotQuestionDTO;
import com.azheng.boot.statistics.dto.RealtimeEventDTO;
import com.azheng.boot.statistics.service.DashboardService;
import com.azheng.framework.web.Result;
import com.azheng.framework.web.Results;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 数据大屏控制器
 * 提供数据概览、趋势分析、实时监控等API接口
 */
@Slf4j
@RestController
@RequestMapping("/agent/statistics/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取核心指标数据
     *
     * @param timeRange 时间范围：24h / 7d / 30d
     * @return KPI指标数据
     */
    @GetMapping("/kpi")
    public Result<KPIDTO> getKPI(@RequestParam(defaultValue = "24h") String timeRange) {
        log.info("获取KPI指标数据, 时间范围: {}", timeRange);
        return Results.success(dashboardService.getKPI(timeRange));
    }

    /**
     * 获取趋势数据
     *
     * @param timeRange 时间范围：24h / 7d / 30d
     * @param type      数据类型：session(会话) / message(消息) / response(响应时间)
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public Result<TrendDTO> getTrend(
            @RequestParam(defaultValue = "24h") String timeRange,
            @RequestParam(defaultValue = "session") String type) {
        log.info("获取趋势数据, 时间范围: {}, 类型: {}", timeRange, type);
        return Results.success(dashboardService.getTrend(timeRange, type));
    }

    /**
     * 获取实时事件流（SSE）
     * 用于推送实时会话、消息等事件
     *
     * @return SSE事件发射器
     */
    @GetMapping(value = "/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getRealtimeEvents() {
        log.info("建立实时事件流连接");
        return dashboardService.createRealtimeEmitter();
    }

    /**
     * 获取热点问题排行
     *
     * @param limit 返回数量限制，默认10
     * @return 热点问题列表
     */
    @GetMapping("/hot-questions")
    public Result<List<HotQuestionDTO>> getHotQuestions(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("获取热点问题排行, 数量: {}", limit);
        return Results.success(dashboardService.getHotQuestions(limit));
    }

    /**
     * 获取实时会话列表
     *
     * @param limit 返回数量限制，默认20
     * @return 实时会话列表
     */
    @GetMapping("/realtime-sessions")
    public Result<List<RealtimeEventDTO>> getRealtimeSessions(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("获取实时会话列表, 数量: {}", limit);
        return Results.success(dashboardService.getRealtimeSessions(limit));
    }

    /**
     * 获取消息来源分布
     *
     * @param timeRange 时间范围：24h / 7d / 30d
     * @return 消息来源分布数据
     */
    @GetMapping("/message-distribution")
    public Result<List<TrendDTO.DistributionItem>> getMessageDistribution(
            @RequestParam(defaultValue = "24h") String timeRange) {
        log.info("获取消息来源分布, 时间范围: {}", timeRange);
        return Results.success(dashboardService.getMessageDistribution(timeRange));
    }
}
