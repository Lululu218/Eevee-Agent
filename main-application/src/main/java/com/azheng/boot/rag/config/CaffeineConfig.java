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
package com.azheng.boot.rag.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.model.chat.response.StreamingHandle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {
    /* 配置参数说明：
     * initialCapacity=[integer]: 初始的缓存空间大小
     * maximumSize=[long]: 缓存的最大条数
     * maximumWeight=[long]: 缓存的最大权重
     * expireAfterAccess=[duration]: 最后一次写入或访问后经过固定时间过期
     * expireAfterWrite=[duration]: 最后一次写入后经过固定时间过期
     * refreshAfterWrite=[duration]: 创建缓存或者最近一次更新缓存后经过固定的时间间隔，刷新缓存
     * weakKeys: 打开key的弱引用
     * weakValues：打开value的弱引用
     * softValues：打开value的软引用
     * recordStats：开发统计功能
     * 注意：
     * expireAfterWrite和expireAfterAccess同事存在时，以expireAfterWrite为准。
     * maximumSize和maximumWeight不可以同时使用
     * weakValues和softValues不可以同时使用
    * */


    /**
     *

     */
    /**
     * 【流式请求专用缓存】
     * key: requestId (前端生成UUID)
     * value: StreamingHandle (LangChain4j流式句柄)
     */
    @Bean
    public Cache<String, StreamingHandle> streamHandleCache() {
        return Caffeine.newBuilder()
                // 初始缓存空间大小
                .initialCapacity(50)
                // 写入5分钟后自动过期
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 限制最大缓存请求（防止内存溢出），超过最大请求数时会采用LRU算法驱逐
                .maximumSize(1000)
                // 缓存移除时监听（自动清理资源）
                .removalListener((key, value, cause) -> {
                    if (value != null) {
                        ((StreamingHandle) value).cancel();
                    }
                })
                // 记录命中
                .recordStats()
                .build();
    }
}
