package com.azheng.mcp.config;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * MCP服务器配置
 */
@Configuration
public class McpServerConfig {

    private static final String BAI_DU_API_KEY = "jGRqpaD7F24iQ9egTNAFd8YmwZFf6l8V";


    /**
     * 百度地图所供应的 MCP服务仓库
     */
    @Bean
    public McpClient BaiduMcpClient() {

        String url = String.format("https://mcp.map.baidu.com/mcp?ak=%s", BAI_DU_API_KEY);

        //1.Transport
        StreamableHttpMcpTransport streamableHttpMcpTransport = StreamableHttpMcpTransport
                .builder()
                .url(url)
                .build();

        //2.Client(可多个)
        /**
         * 多个 McpClient 的意义
         * 多服务提供商
         * 负载均衡/高可用
         * 环境隔离
         */
        return DefaultMcpClient.builder()
                .transport(streamableHttpMcpTransport)
                .key("eevee-agent-mcp-v1")
                .clientName("eevee-agent")
                .clientVersion("1.0.0")
                .initializationTimeout(Duration.ofSeconds(30))
                .toolExecutionTimeout(Duration.ofSeconds(45))
                .resourcesTimeout(Duration.ofSeconds(60))
                .autoHealthCheck(true)
                .autoHealthCheckInterval(Duration.ofSeconds(20))
                .reconnectInterval(Duration.ofSeconds(5))
                .cacheToolList(true)
                .build();

    }
}
