# 基于Langchian4j实现MCP

## 基础概念

### **1.是什么**

MCP（ Model Context Protocol ）模型 上下文 协议。天生就是**标准客户端 - 服务器（C/S）架构**，一问一答请求响应模式，**服务端被动等待、客户端主动发起所有交互**。

官方解释是：是一个开源标准，用于将人工智能应用与外部系统连接起来。

MCP是一种协议，是AI应用 与 外部系统/服务 交互的标准化方式，像Cursor、Trae这样的智能体应用可以连接数据源、工具等，能给AI应用增加配件，赋能生产力

![img](./../../../../images/mcp-simple-diagram.png)



### **2.能干嘛？**

- 你的智能客服可以访问电商系统接口，管理商品订单等
- 企业内部聊天助手可以访问ERP系统数据，更好的回答企业内部问题，分析数据
- 代码助手可以基于另一个设计工具或Agent如专属的AI短剧助手，去设计生成AI短剧的剧本，场景图甚至一部短剧



### **3.优点**

MCP 是一种开放协议，支持广泛的客户端和服务器。



### **4.通信协议类型**

**官方提供类型：**

- **流式HTTP：** 客户端发起 HTTP 请求，服务端可返回普通响应；如需持续分批推送多条响应数据，服务端则会开启 SSE 数据流。
- **stdio（标准输入输出） **：客户端可将 MCP 服务端启动为本地子进程，通过标准输入、标准输出与其直接通信。

**Langchian4j额外提供：**

- **WebSocket 传输：**但目前仅适配 Quarkus MCP 服务端扩展所实现的 WebSocket ，其他暴露的WebSocket接口无法保证兼容性
- **Docker Stdio：**能够直接调用打包为容器镜像、基于 stdio 运行的 MCP 服务端。





## 构建MCP服务器

**前置依赖：**

```xml
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-mcp</artifactId>
        </dependency>
```





### 1.MCP Transport

> 负责**怎么发**底层网络通信

![image-20260519204529619](./../../../../images/image-20260519204529619.png)



**url()：**对方系统的 MCP 网关入口，比如四三房

🎯 核心参数

| 参数            | 说明                         | 使用场景                         |
| :-------------- | :--------------------------- | -------------------------------- |
| timeout()       | HTTP 客户端连接/读取超时时间 | 默认 `60s`                       |
| customHeaders() | 自定义 HTTP 请求头           | **鉴权必配**                     |
| logRequests()   | 日志打印请求JSON             | **开发调试必备**                 |
| logResponses()  | 日志输出请求JSON             | **开发调试必备**                 |
| executor()      | 自定义异步线程池             | 默认使用 `HttpClient` 内置线程池 |

### 2.MCP Client

> 管 **发什么/怎么管**（MCP 协议状态/生命周期）

🔑 核心参数

| 参数名            | 类型           | 默认值              | 说明/作用                                       |
| :---------------- | :------------- | :------------------ | :---------------------------------------------- |
| `transport`       | `McpTransport` | **无（必填）**      | 底层通信实例（如 `StreamableHttpMcpTransport`） |
| `key`             | `String`       | `UUID.randomUUID()` | 客户端唯一标识，用于路由/追踪                   |
| `clientName`      | `String`       | `"langchain4j"`     | 初始化时向服务端声明的客户端名称                |
| `clientVersion`   | `String`       | `"1.0"`             | 客户端版本号                                    |
| `protocolVersion` | `String`       | `"2025-11-25"`      | MCP 协议版本声明                                |

⏱️ 超时控制参数

| 参数名                  | 类型       | 默认值 | 说明/作用                      | 使用建议                                                     |
| :---------------------- | :--------- | :----- | :----------------------------- | :----------------------------------------------------------- |
| `initializationTimeout` | `Duration` | `30s`  | 握手初始化最大等待时间         | 服务端启动慢或跨区网络可调至 `45s~60s`                       |
| `toolExecutionTimeout`  | `Duration` | `60s`  | 执行 MCP 工具的超时时间        | 复杂工具（DB/外部API）建议 `30s~120s`；设为 `0` 表示永不超时 |
| `resourcesTimeout`      | `Duration` | `60s`  | 获取/读取资源（Resources）超时 | 大文件或慢查询                                               |



🔄 连接与高可用参数

| 参数名                    | 类型       | 默认值 | 说明/作用                      | 使用建议                            |
| :------------------------ | :--------- | :----- | :----------------------------- | :---------------------------------- |
| `autoHealthCheck`         | `boolean`  | `true` | 是否启用后台自动 Ping 健康检查 | **生产建议 `true`**，保障长连接活性 |
| `autoHealthCheckInterval` | `Duration` | `30s`  | 健康检查执行间隔               | 建议 `15s~60s`，过频增加服务端负载  |
| `reconnectInterval`       | `Duration` | `5s`   | 连接断开后自动重连等待时间     | 避免 `<2s` 引发重连风暴             |



💾 缓存与行为控制参数

| 参数名          | 类型      | 默认值 | 说明/作用              | 使用建议                                                     |
| :-------------- | :-------- | :----- | :--------------------- | :----------------------------------------------------------- |
| `cacheToolList` | `boolean` | `true` | 是否缓存服务端工具列表 | **强烈建议 `true`**。关闭会导致每次 LLM 调用前都重新拉取列表，显著增加延迟 |



```java
McpClient mcpClient = DefaultMcpClient.builder()
    .key("MyMCPClient")
    .transport(transport)
    .build();
```



### 3.MCP Tool Provider

> # McpToolProvider 核心工作原理（底层设计）
>
> 它是**纯适配层**，**不执行任何工具逻辑**，只做 4 件事：
>
> ### 1. 聚合 MCP 工具
>
> 内部管理所有 `McpClient`，自动调用 `mcpClient.listTools()` 拉取**远程 MCP 服务器的所有工具**（map_weather、map_geocode 等）。
>
> ### 2. 标准化工具格式
>
> 把 MCP 原生工具 → 转换成 **LangChain4j 统一标准工具**`ToolSpecification`
>
> （AiServices 只认识这个格式，不认识原生 MCP 工具）。
>
> ### 3. 绑定工具执行器
>
> 为每个 MCP 工具自动创建 `ToolExecutor`（底层就是封装了 `mcpClient.executeTool(request)`）。
>
> → **让 AiServices 只需调用 executor，不用关心底层是 MCP / 本地工具**。
>
> ### 4. 提供增强能力
>
> 工具过滤、重命名、异常处理、多客户端管理等（你之前不用的封装能力）。

🔑 核心/必配参数

| 参数名                   | 类型                     | 默认值         | 说明/作用                                        |
| :----------------------- | :----------------------- | :------------- | :----------------------------------------------- |
| `mcpClients`             | `List<McpClient>` / 变参 | **无（必填）** | 注册一个或多个已初始化的 `DefaultMcpClient` 实例 |
| `filterToolNames()`      | `String...`              | 无             | 白名单快捷方法，仅暴露指定名称的工具             |
| failIfOneServerFails（） | Boolean                  | 默认为false    | 忽略某个服务器的错误，继续使用其他服务器         |



## 访问MCP资源

1.**编程式访问**

直接调用 MCP 客户端方法。

```java
client.listResources()
client.listPrompts()
client.listTools()
client.listResourceTemplates
```

2.**自动暴露为工具**

通过合成工具，让 LLM 自主调用。







### Tool

服务端暴露的**“可执行函数/动作”**。传参进去，返回结果，能改变状态或拉实时数据。（如：`发送邮件`、`查询订单`、`执行SQL`）

### Resources

服务端能提供的**“只读数据资产”**。就像书架上的文件，只有位置(`URI`)、名字、简介、格式。（如：`app.log`日志、`config.yml`配置、某份合同PDF）



### **Prompt**

服务端统一管理的**“标准化工作指令模板”**。Agent 传参进去，服务端返回拼装好的完整 Prompt，保证全公司用同一套标准。（如：`代码审查模板`、`客诉回复SOP`、`周报生成框架`）



### **ResourceTemplate**

是**“动态生成 Resource 地址的规则”**。资源太多列不全时，用占位符定义路径规则，Agent 自己填参数生成真实 URI 再去读。（如：`file:///logs/{服务名}/{日期}.log`）





## 直接使用 McpClient

### ToolExecutionRequest源码分析

- 它是一个**纯数据载体**（没有任何业务逻辑）
- 你调用 `mcpClient.executeTool(...)` 时，**必须传入它**
- MCP 服务器只认识它，不接受任何自定义参数

```java
    // 1. 请求ID（唯一标识）
    private final String id;
    // 2. 要调用的工具名称（核心！）
    private final String name;
    // 3. 工具参数（核心！必须是JSON字符串）
    private final String arguments;
    
    // 构造器私有 → 只能用 builder() 创建
    private ToolExecutionRequest(Builder builder) {}
    
    // 只有getter，无setter → 不可变对象
```



代码示例：

```java
        // 北京坐标：经度116.4074 在前，纬度39.9042 在后
        String params = "{\"location\":\"116.4074,39.9042\"}";

        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .name("map_weather")
                .arguments(params)
                .build();

        ToolExecutionResult toolExecutionResult = baiduMcpClient.executeTool(request);

```







## 使用AIServices注入Provider

代码示例：	

```java
    Bot bot = AiServices.builder(Bot.class)
        .chatModel(model)
        .toolProvider(toolProvider)
        .build();
    try {
        String response = bot.chat("Summarize the last 3 commits of the LangChain4j GitHub repository");
        System.out.println("RESPONSE: " + response);
    } finally {
        mcpClient.close();
    }
```



优点是大模型会自动根据问题，封装专属格式的工具调用请求体

## 工作原理

正常的交互原理就是：

- 首先封装好工具调用请求对象（具体参照S端规定的格式）
- 传入请求体，通过McpClient去调用工具
- 得到工具请求响应结果
- 关闭连接

lc4j自带的AIServices交互原理：

- 构建McpToolProvider
- 构建AIServices助手
- 绑定McpToolProvider
- 直接传入自然语言问题
- 底层自动选择工具执行

## MCP Server

![image-20260520140855420](./../../../../images/image-20260520140855420.png)

工具大全： https://mcp.so/

通过这个网站我们能看出MCP插件的市场还是很庞大的，AI的开发维度前景还是很广阔的。那我们该如何接入呢



>  **Streamable HTTP**是Anthropic当前在MCP协议中主推用于替代传统SSE的传输方式，相比于SSE它支持无状态通信， 甚至支持按需升级到 SSE，更稳定、更高效的通信也使得它更加适配企业级应用。因此，如果您选择了HTTP远程接入， 在条件允许的情况下我们极力推荐以Streamable HTTP作为接入首选。

前面我们已经了解到，通过流式HTTP链接MCP服务器只需要配置url









## EeveeAgent接入MCP流程

1. 配置MCP Tool Provider
2. 手动构建所需工具的MCP意图节点（如天气查询）
   1. 增加意图类型枚举
   2. 数据库意图节点配置，kind类型增加“MCP”
3. 修改意图识别代码、
   1. 增加filterMcpIntents方法
   2. 修改IntentGroup数据结构
4. 增加MCP工具调用检索通道
   1. 创建
5. 配置专属MCP提示词模版+混合模版
6. 后续进阶维护比如单独封装某个工具插件





## 实战搭建

咱们以搭建一个天气查询工具为例，首先技术选型：

- 采用StreamableHTTP
- 不使用AIServices

1.制定专属天气服务的意图树

2.制定专属天气服务关键词抽取提示词模版

3.根据用户问题提供对应地区的行政区编码六位

4.设置默认的请求参数做备用，或用另一个获取定位的插件做兜底；如果当前的用户未指明地区，无法做解析的话，那就返回友好提示，作为MCP调用结果，然后让最终的提示词去做识别，做友好性引导；还有一个方案就是给当前登录用户绑定一个动态的IP地址，从而获取登录位置

5.封装工具调用请求

6.获取调用结果

7.格式化结果

8.返回

## 百度地图国内天气插件

### 请求参数列表

| 参数名称    | 参数含义                                                     | 默认值 | 字段类型 | 必选 |
| ----------- | ------------------------------------------------------------ | ------ | -------- | ---- |
|             |                                                              |        |          |      |
| district_id | 区县的[行政区划编码](https://mapopen-website-wiki.bj.bcebos.com/cityList/weather_district_id.csv) | 无     | string   | 是   |
| location    | 经纬度，经度在前纬度在后，逗号分隔。支持类型：bd09mc/bd09ll/wgs84/gcj02 | 无     | double   | 是   |
| province    | 省份名称，例如山东省，用于辅助名称查询                       | 无     | string   | 否   |
| city        | 城市名称，例如济南市，用于辅助名称查询                       | 无     | string   | 否   |
| district    | 区县名称，例如历下区，用于名称查询，根据区县名称查询对应区县的天气数据，和district_id以及location三者必填其一 | 无     | string   | 否   |
| ak          | 开发者密钥，可在[API控制台](http://lbsyun.baidu.com/apiconsole/key)申请获得 | 无     | string   | 是   |
| data_type   | 请求数据类型。数据类型有：now/fc/index/alert/fc_hour/all，控制返回内容 | 无     | string   | 是   |
| output      | 返回格式，目前支持json/xml                                   | json   | string   | 否   |
| coordtype   | 支持类型:wgs84/bd09ll/bd09mc/gcj02                           | wgs84  | string   | 否   |



### data_type数据类型解析：

| 取值      | 全称 / 核心含义 | 具体说明                                                     |
| --------- | --------------- | ------------------------------------------------------------ |
| `now`     | **实况天气**    | 获取**当前 / 实时**的天气数据（比如此刻的温度、湿度、风向、天气状况），是 “此刻” 的天气快照，也包含今天的实时统计数据。 |
| `fc`      | **按天预报**    | 获取**未来多天**的天气数据（通常是未来 3-7 天 / 10 天），以 “天” 为单位，包含每天的白天 / 夜间天气、最高 / 最低温、降水概率等。 |
| `index`   | **生活指数**    | 获取天气相关的生活建议指数（比如紫外线、穿衣、洗车、感冒指数），和 “今天 / 明天” 的时间维度无关，属于辅助类数据。 |
| `alert`   | **天气预警**    | 获取当前生效的官方天气预警（比如暴雨、台风、高温预警），属于事件类数据，不直接对应 “今天 / 明天” 的时间维度。 |
| `fc_hour` | **逐小时预报**  | 获取**未来逐小时**的天气数据（通常是未来 24 小时 / 72 小时），以 “小时” 为单位，比如未来 1 小时、3 小时、6 小时的温度、降水预报。 |
| `all`     | **所有数据**    | 一次性获取以上所有类型的数据（实况 + 按天预报 + 逐小时预报 + 指数 + 预警）。 |
