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

/**
 * 模型厂商路由规范
 * 获取指定场景可用的模型厂商
 */
public interface AiModelRouter {
    /**
     * 1.流式输出路由场景
     */
    StreamingChatModel routerStreamingCHatModel(Boolean isThinking);

    /**
     * 2.后台基础工作路由场景
     */
    ChatModel routerChatModel();

    /**
     * 3.文本向量化路由场景
     */
    EmbeddingModel routerEmbeddingModel();

    /**
     * 手动切换路由策略
     * （若只有一种路由策略比如优先级那就可以不用该方法）
     */
    void switchStrategy(RouteStrategy strategy);
}
