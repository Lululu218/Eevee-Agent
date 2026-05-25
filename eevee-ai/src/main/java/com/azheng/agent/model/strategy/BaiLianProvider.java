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
package com.azheng.agent.model.strategy;

import com.azheng.agent.model.AiProvider;
import com.azheng.agent.model.AiProviderProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class BaiLianProvider implements AiProvider {
    private final AiProviderProperties properties;
    
    /**
     * 获取百炼厂商配置（延迟初始化）
     */
    private AiProviderProperties.ProviderConfig getConfig() {
        return properties.getBaiLian();
    }

    @Value(value = "${ai.dimensions}")
    private Integer dimensions;


    @Override
    public StreamingChatModel getStreamingChatModel(Boolean isThinking) {
        return OpenAiStreamingChatModel
                .builder()
                .apiKey(getConfig().getApiKey())
                .baseUrl(getConfig().getBaseUrl())
                .modelName(getConfig().getStreamModel())
                .temperature(0.7)
                .sendThinking(isThinking)
                .returnThinking(true)
                .topP(0.9)
                .maxTokens(2048)
                .frequencyPenalty(0.1)
                .presencePenalty(0.1)
                .build();
    }

    @Override
    public ChatModel getBackgroundModel() {
        return OpenAiChatModel
                .builder()
                .apiKey(getConfig().getApiKey())
                .baseUrl(getConfig().getBaseUrl())
                .modelName(getConfig().getStreamModel())
                .temperature(0.1)
                .topP(0.8)
                .maxTokens(300)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .build();
    }

    @Override
    public EmbeddingModel getEmbeddingModel() {
        return OpenAiEmbeddingModel
                .builder()
                .apiKey(getConfig().getApiKey())
                .baseUrl(getConfig().getBaseUrl())
                .modelName(getConfig().getStreamModel())
                .dimensions(dimensions)
                .build();
    }
}
