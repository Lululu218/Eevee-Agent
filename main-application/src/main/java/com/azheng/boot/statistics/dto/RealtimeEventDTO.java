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

import java.time.LocalDateTime;

/**
 * 实时事件数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeEventDTO {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 事件类型：SESSION_START / MESSAGE_SENT / MESSAGE_RECEIVED / SESSION_END
     */
    private String eventType;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名/昵称
     */
    private String userName;

    /**
     * 消息内容（摘要）
     */
    private String messageContent;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 会话状态：ACTIVE / ENDED
     */
    private String status;

    /**
     * 消息来源：DIRECT / KNOWLEDGE_BASE
     */
    private String sourceType;

    /**
     * 响应时间(ms)
     */
    private Long responseTime;
}
