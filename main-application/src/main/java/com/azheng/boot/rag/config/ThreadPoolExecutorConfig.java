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

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置中心
 */
@Configuration
public class ThreadPoolExecutorConfig {

    /**
     * CPU核心数，用于动态计算线程池大小
     */
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 意图识别并行处理线程池
     * @return
     */
    @Bean
    public Executor intentClassifyThreadPoolExecutor(){
        ThreadPoolExecutor intentClassifyExecutor = new ThreadPoolExecutor(
                CPU_COUNT,                          // 核心线程数：设置为 CPU 核心数，避免频繁上下文切换
                CPU_COUNT << 1,                     // 最大线程数：核心数的 2 倍，利用超线程特性应对突发流量
                60,                                 // 空闲线程存活时间：60 秒，超过核心数的空闲线程超时后回收
                TimeUnit.SECONDS,                   // 时间单位：秒
                new SynchronousQueue<>(),           // 工作队列：SynchronousQueue 不存储任务，直接创建新线程执行
                ThreadFactoryBuilder.create()       // 线程工厂：使用 Hutool的 ThreadFactoryBuilder 自定义线程名
                        .setNamePrefix("intent_classify_executor")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程执行任务，保证任务不丢失且平滑降级
        );
        // 包装为 TTL 线程池，确保 ThreadLocal 上下文（如分布式链路追踪 ID、用户信息）在异步任务中传递
        return TtlExecutors.getTtlExecutor(intentClassifyExecutor);
    }


    @Bean
    public Executor ragContextThreadPoolExecutor(){
        ThreadPoolExecutor ragContextExecutor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create()
                        .setNamePrefix("rag_context_executor_")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(ragContextExecutor);
    }

    /**
     * 异步SSE流式输出
     * @return
     */
    @Bean
    public Executor streamChatThreadPoolExecutor(){
        ThreadPoolExecutor streamChatExecutor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create()
                        .setNamePrefix("stream_chat_executor_")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(streamChatExecutor);
    }

    /**
     * 异步执行LLM对话持久化操作
     */
    @Bean
    public Executor treatResponseThreadPoolExecutor(){
        ThreadPoolExecutor treatResponseExecutor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                ThreadFactoryBuilder.create()
                        .setNamePrefix("treat_response_executor_")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(treatResponseExecutor);
    }

    /**
     * RAG 检索线程池（用于通道级别的并行）
     */
    @Bean
    public Executor ragRetrievalThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT << 1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryBuilder.create()
                        .setNamePrefix("rag_retrieval_executor_")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * RAG 内部检索线程池
     */
    @Bean
    public Executor ragInnerRetrievalThreadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_COUNT << 1,
                CPU_COUNT << 2,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                ThreadFactoryBuilder.create()
                        .setNamePrefix("rag_inner_retrieval_executor_")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return TtlExecutors.getTtlExecutor(executor);
    }


}
