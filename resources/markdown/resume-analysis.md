# EeveeAgent 简历分析代码证据库

## 1. 项目整体定位

从代码看，EeveeAgent 是一个面向企业内部知识问答的 RAG 应用：后端采用 Spring Boot 多模块结构，提供用户认证、知识库管理、文档上传入库、向量检索、意图识别、SSE 流式问答和会话持久化；前端采用 Vue/Vite，提供登录、知识库/文档/知识块管理、意图树管理和聊天界面。项目核心数据落在 MySQL，原始文件进入 RustFS，向量数据进入 Milvus，会话记忆和意图树缓存使用 Redis。

## 2. 技术栈确认

### 2.1 代码中明确确认使用的技术

| 技术 | 出现位置 | 作用 |
| --- | --- | --- |
| Java 17 | `pom.xml` | 父工程配置 `java.version=17`。 |
| Spring Boot / Spring MVC | `main-application/src/main/java/com/azheng/boot/EeveeApplication.java`；各 Controller | 主应用启动、REST API、SSE 接口。 |
| Maven 多模块 | `pom.xml` | 父工程聚合 `main-application`、`core-framework`、`eevee-ai`。 |
| MyBatis-Plus | `core-framework/src/main/java/com/azheng/framework/config/DataBaseConfiguration.java`；各 `*Mapper` | ORM、分页插件、BaseMapper CRUD。 |
| MySQL | `main-application/src/main/resources/application.yaml`；`resources/database/schema.sql` | 用户、知识库、文档、分块、会话、意图树等结构化数据存储。 |
| Sa-Token | `user/config/SaTokenConfig.java`；`AuthServiceImpl.java`；`UserController.java` | 登录态校验、角色校验、登录/登出。 |
| Redis / StringRedisTemplate | `RedisChatMemoryStore.java`；`HistoryAbstractStore.java`；`IntentTreeCacheManager.java` | 会话记忆、历史摘要、意图树缓存。 |
| TransmittableThreadLocal | `core-framework/src/main/java/com/azheng/framework/context/UserContext.java` | 当前用户上下文保存。 |
| LangChain4j | `LLMConfig.java`；`QueryTransformerService.java`；`LangChain4jParser.java`；`LangChain4jSplitter.java` | LLM、Embedding、Query Rewrite、Tika 解析、文档分块、ChatMemory。 |
| DashScope/OpenAI-compatible API | `LLMConfig.java`；`application.yaml` | 使用 DashScope 兼容 OpenAI 的 base-url 构造 `qwen-flash`、`qwen-turbo`、`text-embedding-v4`。 |
| DashScope Rerank SDK | `DashScopeRerankService.java` | 调用 `TextReRank` 的 `GTE_RERANK_V2` 做重排。 |
| Milvus Java SDK v2 | `MilvusClientConfig.java`；`MilvusOperations.java`；`VectorEmbedding.java` | Collection 创建、向量写入、向量检索、Upsert/Delete。 |
| RustFS / S3 SDK | `RustFSClientConfig.java`；`KnowledgeDocumentServiceImpl.java`；`LangChain4jParser.java` | 文件上传到 S3 兼容对象存储、按 key 读取文件流解析。 |
| Apache Tika via LangChain4j | `LangChain4jParser.java` | 使用 `ApacheTikaDocumentParser` 解析上传文档。 |
| Caffeine Cache API | `RAGChatServiceImpl.java` | 注入 `Cache<String, StreamingHandle>` 保存流式输出句柄用于取消；当前代码未发现对应 Bean 定义。 |
| Vue 3 / Vite | `frontend/package.json`；`frontend/vite.config.js` | 前端页面和本地开发代理。 |
| Axios | `frontend/src/main.js`；`Admin.vue`；`Home.vue`；`Login.vue` | 前端 HTTP 请求封装和调用后端接口。 |
| EventSource / SSE | `frontend/src/components/Home.vue`；`RAGChatController.java` | 前端接收流式输出，后端返回 `SseEmitter`。 |

### 2.2 配置中出现但代码调用不明显的技术

| 技术 | 配置文件位置 | 可能作用 / 当前确认情况 |
| --- | --- | --- |
| Redisson | `pom.xml`、`core-framework/pom.xml` | 依赖已引入，当前代码未发现直接调用。 |
| Spring AI | `pom.xml` dependencyManagement | BOM 已引入，当前代码未发现 Spring AI API 使用；实际 LLM 调用来自 LangChain4j。 |
| jmimemagic | `main-application/pom.xml` | 依赖已引入，但文件校验实际使用 `MultipartFile.getContentType()` 和扩展名，未发现 jmimemagic 调用。 |
| `rag.default.*` | `main-application/src/main/resources/application.yaml` | 配置了默认 collection、dimension、metric-type；当前创建 Collection 和检索主要使用 `KnowledgeBasePO.collection`，默认 collection 未发现主链路调用。 |
| `intent.topk` | `application.yaml` | 配置存在，当前 Java 代码未发现读取。 |
| Milvus `keepAliveTime`、`idleTimeout` | `application.yaml` | 配置存在，但 `MilvusClientConfig.connect()` 中 idle/keepAlive 使用硬编码值。 |
| RustFS `rustfs.*` | `application.yaml` | 配置存在，但 `RustFSClientConfig.RustFSClient()` 中 endpoint 和账号密码硬编码。 |
| Attu | `resources/docker/milvus-stack-2.6.6.compose.yaml` | Milvus 管理界面容器，只在 Docker 配置出现。 |

### 2.3 未确认但可能相关的技术

- Elasticsearch：`SearchChannelType.KEYWORD_ES` 仅为枚举值，未发现实现类。
- MCP：`IntentGroup` 注释和 `IntentResolver` 中有 MCP 相关注释代码，未发现可运行实现。
- Spring AI：只见依赖管理，未发现实际使用。
- OpenAI 官方服务：代码使用 `OpenAi*Model` 客户端，但 base-url 配置为 DashScope 兼容接口，不能写成已接入 OpenAI 官方服务。
- RocketMQ、XXL-Job：仅在 prompt 文案示例中出现，代码中未确认接入。
- 异步任务状态中心：文档分块注释中提到后续异步，当前代码未发现任务状态表或状态机。

## 3. 核心功能清单

### 知识库创建、重命名、删除、分页查询

- 是否已实现：已实现
- 相关文件：
  - `main-application/src/main/java/com/azheng/boot/kb/controller/KnowledgeBaseController.java`
  - 类名：`KnowledgeBaseController`
  - 方法名：`createKnowledgeBase`、`deleteKnowledgeBase`、`updateKnowledgeBase`、`pageQuery`
  - `main-application/src/main/java/com/azheng/boot/kb/service/Impl/KnowledgeBaseServiceImpl.java`
  - 类名：`KnowledgeBaseServiceImpl`
  - 方法名：`create`、`delete`、`update`、`pageQuery`
- 核心逻辑：
  - 创建时校验请求参数，写入 `knowledge_base`，并调用 `MilvusOperations.createChunkCollection()` 创建同名 Milvus Collection。
  - 删除时逻辑删除知识库、其下文档、其下知识块；Milvus Collection 删除只有注释，当前未执行。
  - 重命名时检查知识库存在、名称非空、名称不重复，再更新 `kbName` 和 `updateBy`。
  - 分页查询按 `delFlag=0`、`kbName like`、`updateTime desc` 查询。
- 可写入简历程度：推荐写
- 原因：
  - CRUD 和 Milvus Collection 创建链路有明确代码；删除 Milvus Collection 未实现，写简历时不要写“完整物理删除向量库”。

### 文档上传、文件存储与元数据入库

- 是否已实现：已实现
- 相关文件：
  - `KnowledgeDocumentController.java`
  - 类名：`KnowledgeDocumentController`
  - 方法名：`upload`
  - `KnowledgeDocumentServiceImpl.java`
  - 类名：`KnowledgeDocumentServiceImpl`
  - 方法名：`uploadFile`
  - `FileValidationService.java`
  - 类名：`FileValidationService`
  - 方法名：`validateFile`
  - `RustFSClientConfig.java`
  - 类名：`RustFSClientConfig`
  - 方法名：`RustFSClient`
- 核心逻辑：
  - 前端上传 multipart 文件和 `kbId`。
  - 后端校验文件大小、MIME/扩展名、可读性、是否疑似加密。
  - 使用 S3Client 将文件流写入 RustFS bucket `a-bucket`，key 为 `uploadFile/{uuid}/{filename}`。
  - 写入 `knowledge_document` 记录，并写入 `file_to_rustfs_log` 保存 docId 到 RustFS key 的映射。
- 可写入简历程度：推荐写
- 原因：
  - 存储链路清楚，MySQL 元数据和对象存储映射表都有表结构和代码。

### 文档解析、分块、向量化、写入 Milvus

- 是否已实现：已实现
- 相关文件：
  - `KnowledgeDocumentController.java`
  - 方法名：`runChunk`
  - `KnowledgeDocumentServiceImpl.java`
  - 方法名：`startChunking`
  - `knowledgeChunkServiceImpl.java`
  - 方法名：`performChunking`
  - `LangChain4jParser.java`
  - 方法名：`TikaParser`
  - `LangChain4jSplitter.java`
  - 方法名：`splitterToChunk`
  - `VectorEmbedding.java`
  - 方法名：`embed`
- 核心逻辑：
  - 根据 docId 查文档和 RustFS key，从 RustFS 获取文件流。
  - 使用 LangChain4j `ApacheTikaDocumentParser` 解析为 `Document`。
  - 给文档 metadata 添加 `docId`、`file_name`。
  - 使用 `DocumentSplitters.recursive(maxChunkSize, maxOverlapSize)` 切分为 `TextSegment`。
  - 使用 `EmbeddingModel` 对每个 segment 做 embedding，写入 Milvus 字段 `chunk_text_vector`、`chunk_text_varchar`、`metadata`。
  - 将返回的 Milvus 主键与 chunk 文本、chunkIndex、字符数、估算 token 数写入 `knowledge_chunk`。
- 可写入简历程度：推荐写
- 原因：
  - RAG 入库链路完整可追踪；但当前是同步执行，无任务状态与重试，不建议写成“异步高可靠入库流水线”。

### 知识块管理

- 是否已实现：已实现
- 相关文件：
  - `KnowledgeChunkController.java`
  - 方法名：`queryByDocId`、`updateText`、`createChunk`、`deleteChunk`
  - `knowledgeChunkServiceImpl.java`
  - 方法名：`pageQuery`、`updateChunkText`、`insertChunk`、`remove`
- 核心逻辑：
  - 支持按 docId 分页查询 chunk。
  - 支持手工新增单条 chunk：文本转 `TextSegment`，embedding 后写 Milvus，再写 MySQL。
  - 支持编辑 chunk：先更新 MySQL 文本，再调用 `MilvusOperations.updateEntity()` upsert 向量记录。
  - 支持删除 chunk：当前逻辑删除 MySQL，Milvus 删除代码被注释。
- 可写入简历程度：推荐写
- 原因：
  - 增查改删均有代码；删除 Milvus 未执行，简历中不要写“全链路一致删除”。

### 知识库与文档关系

- 是否已实现：已实现
- 相关文件：
  - `KnowledgeBasePO.java`
  - `KnowledgeDocumentPO.java`
  - `KnowledgeChunkPO.java`
  - `resources/database/schema.sql`
- 核心逻辑：
  - `knowledge_document.kb_id` 关联知识库。
  - `knowledge_chunk.kb_id`、`knowledge_chunk.doc_id` 同时关联知识库和文档。
  - `file_to_rustfs_log.doc_id` 记录文档到 RustFS object key 的映射。
- 可写入简历程度：推荐写
- 原因：
  - 表结构和 PO 映射明确。

### 知识库权限隔离或用户隔离

- 是否已实现：部分实现
- 相关文件：
  - `KnowledgeBaseServiceImpl.java`
  - 方法名：`create`、`pageQuery`、`delete`、`update`
  - `KnowledgeDocumentServiceImpl.java`
  - 方法名：`uploadFile`、`docPageQuery`
  - `SaTokenConfig.java`
  - 方法名：`addInterceptors`
- 核心逻辑：
  - 全局 Sa-Token 拦截器要求登录。
  - 创建知识库/文档时写入 `createBy=UserContext.getUsername()`。
  - 知识库分页、删除、重命名、文档查询未按当前用户过滤，也未发现知识库 owner 权限判断。
- 可写入简历程度：谨慎写
- 原因：
  - 可以写“接入登录态和操作人记录”，不建议写“实现知识库级多租户隔离”。

### RAG SSE 流式问答

- 是否已实现：已实现
- 相关文件：
  - `RAGChatController.java`
  - 方法名：`streamingChat`
  - `RAGChatServiceImpl.java`
  - 方法名：`streamingChat`、`LLMChatResponse`
  - `LLMConfig.java`
  - 方法名：`buildQwenFlashStreamingModel`
  - `frontend/src/components/Home.vue`
  - 方法名：`sendMessage`、`regenerateResponse`
- 核心逻辑：
  - 前端通过 `EventSource` GET `/agent/rag/streamingChat`，传 `prompt`、`roundId`、可选 `conversationId`、`openThinking`。
  - 后端创建 `SseEmitter`，新会话先发送 `init` 事件返回真实 `conversationId`。
  - 使用 `OpenAiStreamingChatModel` 调用 DashScope `qwen-flash`。
  - 在 `onPartialResponse` 中逐段 `emitter.send()`，在 `onCompleteResponse` 中异步执行持久化和收尾。
- 可写入简历程度：推荐写
- 原因：
  - 主链路明确；但取消依赖的 `streamHandleCache` Bean 当前代码未发现，运行层面有风险。

### 查询改写

- 是否已实现：已实现
- 相关文件：
  - `QueryTransformerService.java`
  - 方法名：`buildRewriteResult`、`queryCompressRewrite`、`queryExpandingRewrite`
- 核心逻辑：
  - 使用 LangChain4j `CompressingQueryTransformer` 基于上下文压缩用户问题。
  - 使用 `ExpandingQueryTransformer` 扩展子问题。
  - 返回 `RewriteResult(rewrittenQuestion, subQuestions)`。
- 可写入简历程度：推荐写
- 原因：
  - 代码调用明确，适合写“基于 LangChain4j 做 query compression/expansion”。

### LLM 意图识别与意图树管理

- 是否已实现：已实现
- 相关文件：
  - `IntentResolver.java`
  - 方法名：`resolve`、`classifyIntents`、`mergeIntentGroup`
  - `DefaultIntentClassifier.java`
  - 方法名：`classifyTargets`、`buildIntentPromptTemplate`、`loadIntentTreeFromDB`
  - `IntentTreeCacheManager.java`
  - 方法名：`getIntentTreeFromCache`、`saveIntentTreeToCache`、`clearIntentTreeCache`
  - `IntentTreeController.java`
  - 方法名：`tree`、`createNode`、`updateNode`、`deleteNode`、`batchEnable`、`batchDisable`、`batchDelete`
  - `IntentTreeServiceImpl.java`
  - 方法名：`getFullTree`、`createNode`、`updateNode`、`deleteNode`、`batchEnableNodes`、`batchDisableNodes`、`batchDeleteNodes`
- 核心逻辑：
  - 意图节点存储在 `intent_node` 表，支持树形查询和节点 CRUD。
  - 分类器启动时确保 Redis 中有意图树缓存，缓存缺失时从 DB 加载。
  - 每次分类从 Redis/DB 加载叶子节点，渲染 `prompt/Intent-recognition.st`，调用 LLM 输出 JSON 分数，再转为 `NodeScore`。
  - `IntentResolver` 对每个子问题并行分类，过滤低分意图并限制总意图数量。
- 可写入简历程度：推荐写
- 原因：
  - 意图树管理、缓存和 LLM 分类都有明确实现；但启用字段过滤在分类加载中未发现，需谨慎表述。

### 多路召回

- 是否已实现：部分实现
- 相关文件：
  - `MultiChannelRetrievalEngine.java`
  - 方法名：`retrieveKnowledgeChannels`、`executeSearchChannels`、`executePostProcessors`
  - `SearchChannel.java`
  - `IntentDirectedSearchChannel.java`
  - 方法名：`isEnabled`、`search`
  - `VectorGlobalSearchChannel.java`
  - 方法名：`isEnabled`、`search`
- 核心逻辑：
  - Spring 注入所有 `SearchChannel` 实现，按 `isEnabled` 和 priority 选择通道。
  - 当前发现两个通道：意图定向检索、低置信/无意图时的全局向量检索。
  - 通道并行执行，结果合并后进入后处理器链。
- 可写入简历程度：谨慎写
- 原因：
  - “多通道检索框架”成立；但当前未发现 BM25/ES/关键词检索实现，不能写“混合检索（向量+关键词+ES）已落地”。

### 向量检索

- 是否已实现：已实现
- 相关文件：
  - `RecallService.java`
  - 方法名：`recallMilvusContentWithKB`、`recallMilvusContentWithGlobal`
  - `MilvusOperations.java`
  - 方法名：`search`
- 核心逻辑：
  - 将问题通过 `EmbeddingModel.embed()` 转为向量。
  - 使用 Milvus `SearchReq`，指定 `annsField=chunk_text_vector`、`outputFields=chunk_text_varchar, metadata`、`ConsistencyLevel.EVENTUALLY`。
  - 返回 chunkId、文本、metadata、相似度分数。
- 可写入简历程度：推荐写
- 原因：
  - 检索代码清楚。

### Rerank

- 是否已实现：已实现
- 相关文件：
  - `RerankPostProcessor.java`
  - 方法名：`process`
  - `DashScopeRerankService.java`
  - 方法名：`rerank`
- 核心逻辑：
  - 后处理器链始终启用 Rerank。
  - 将候选 chunk 文本传入 DashScope `TextReRank`，模型为 `GTE_RERANK_V2`，按 `topN` 返回重排结果。
- 可写入简历程度：推荐写
- 原因：
  - 调用链明确；需要注意重排后 `ReRankContext.id` 使用的是 rerank 结果 index，不是原始 Milvus id。

### Prompt 拼接

- 是否已实现：已实现
- 相关文件：
  - `RAGPromptService.java`
  - 方法名：`buildStructuredMessages`、`buildSystemPrompt`、`planKbOnly`、`planPrompt`
  - `DefaultContextFormatter.java`
  - 方法名：`formatKbContext`
  - `PromptTemplateLoader.java`
  - 方法名：`load`、`render`
  - `PromptTemplateUtils.java`
  - 方法名：`fillSlots`、`cleanupPrompt`
  - `main-application/src/main/resources/prompt/answer-chat-kb.st`
  - `main-application/src/main/resources/prompt/answer-chat-system.st`
- 核心逻辑：
  - 根据是否有 KB 上下文选择 RAG 模板。
  - 单意图且节点有 `promptTemplate` 时使用节点模板，否则使用默认 RAG 模板。
  - `DefaultContextFormatter` 将意图片段和 rerank 后 chunk 拼成知识库上下文。
  - 最终消息包括 SystemMessage、包含文档内容的 UserMessage、用户问题 UserMessage。
- 可写入简历程度：推荐写
- 原因：
  - Prompt 管理和上下文组装可写；但当前 `buildStructuredMessages` 参数中的 `history` 和 `subQuestions` 没有实际加入最终 RAG messages，不能写“完整多轮上下文注入到 RAG prompt”。

### 取消生成

- 是否已实现：部分实现
- 相关文件：
  - `RAGChatController.java`
  - 方法名：`streamingChatCancellation`
  - `RAGChatServiceImpl.java`
  - 方法名：`cancel`、`LLMChatResponse`
  - `frontend/src/components/Home.vue`
  - 方法名：`cancelStreaming`
- 核心逻辑：
  - 前端生成 `roundId`，取消时调用 `/agent/rag/cancel`。
  - 后端用 `streamHandleCache` 查找 `StreamingHandle` 并调用 `cancel()`。
  - 流式回调中将 `StreamingHandle` 放入缓存，完成/超时/错误时清理。
- 可写入简历程度：谨慎写
- 原因：
  - 业务代码存在；但当前代码未发现 `streamHandleCache` 的 Bean 定义，运行可用性未确认。

### 会话记忆与历史摘要

- 是否已实现：部分实现
- 相关文件：
  - `MemoryStoreConfig.java`
  - 方法名：`originalChatMemoryProvider`、`tokenWindowChatMemoryProvider`
  - `RedisChatMemoryStore.java`
  - 方法名：`getMessages`、`updateMessages`、`deleteMessages`
  - `ConcurrentRedisChatMemory.java`
  - 方法名：`messages`、`add`、`clear`
  - `AbstractCompressService.java`
  - 方法名：`CompressIfNeed`、`judgeThresholdByRounds`
  - `HistoryAbstractStore.java`
  - 方法名：`insert`、`query`、`updateSession`
  - `RAGChatServiceImpl.java`
  - 方法名：`streamingChat`、`treatResponse`
- 核心逻辑：
  - Redis key `EeveeAgent:chat:memory:session:{conversationId}` 保存原始聊天记忆，7 天过期。
  - 超过 6 轮时调用 `qwen-turbo` 生成摘要，写入 `EeveeAgent:chat:memory:abstract:{conversationId}`，并保留最近两轮原始消息。
  - 模型完成后把本轮 user/ai 消息追加到 Redis 记忆。
- 可写入简历程度：谨慎写
- 原因：
  - 记忆与摘要组件存在；但 `RAGChatServiceImpl.streamingChat()` 调 `historyAbstractStore.query(historyMessages)`，传入的是消息列表而不是 conversationId，摘要读取 key 可能不正确；RAG prompt 中也未加入 `history` 参数。

### 消息持久化与会话管理

- 是否已实现：已实现
- 相关文件：
  - `ChatDataPersistenceServiceImpl.java`
  - 方法名：`mysqlPersistent`
  - `ConversationServiceImpl.java`
  - 方法名：`queryConversations`、`mkdirTitle`、`deleteByConversationId`、`queryConversationMessage`、`generateTitle`
  - `ConversationMessageServiceImpl.java`
  - 方法名：`saveConversationMessage`、`queryConversationMessage`、`deleteByMessageId`
  - `ConversationController.java`
  - 方法名：`query`、`updateTitle`、`delete`、`openConversationById`
  - `ConversationMessageController.java`
  - 方法名：`delete`
- 核心逻辑：
  - 首轮对话创建 `conversation` 记录，标题先设为“新对话”。
  - 每轮保存 user 和 ai 两条 `conversation_message`。
  - 完成后调用 `generateTitle()`，用 title summary prompt 生成标题；失败则截取用户问题。
  - 支持按用户查询会话列表、按 conversationId 查询消息、重命名、逻辑删除会话和消息。
- 可写入简历程度：推荐写
- 原因：
  - MySQL 持久化链路清晰；但现有会话重命名、删除、消息查询未看到完整用户归属校验，需要谨慎描述权限。

### 用户登录、认证与角色鉴权

- 是否已实现：部分实现
- 相关文件：
  - `AuthController.java`
  - 方法名：`login`、`logout`
  - `AuthServiceImpl.java`
  - 方法名：`login`、`getUserByName`
  - `SaTokenConfig.java`
  - 方法名：`addInterceptors`
  - `SaTokenStpInterfaceImpl.java`
  - 方法名：`getRoleList`、`getPermissionList`
  - `UserController.java`
  - 方法名：`userPageQuery`、`createUser`、`update`、`delete`
- 核心逻辑：
  - 登录时根据 username 查询 status=1 用户，直接比较明文 password，相等后 `StpUtil.login(userId)` 并返回 token。
  - Sa-Token 拦截除 `/api/auth/**`、`/error` 外全部接口。
  - 管理接口使用 `StpUtil.checkRole("admin")`。
  - `SaTokenStpInterfaceImpl.getRoleList()` 按 loginId 查用户角色，权限列表返回空。
- 可写入简历程度：谨慎写
- 原因：
  - 登录态与角色鉴权实现明确；但密码没有加密校验，权限粒度只到角色，不能写复杂 RBAC。

### 当前用户上下文

- 是否已实现：已实现
- 相关文件：
  - `UserContext.java`
  - 方法名：`set`、`get`、`requireUser`、`getUserId`、`getUsername`、`clear`
  - `UserContextInterceptor.java`
  - 方法名：`preHandle`、`afterCompletion`
  - `UserController.java`
  - 方法名：`currentUser`
- 核心逻辑：
  - 拦截器从 Sa-Token 获取 loginId，查用户表，构造 `LoginUser` 放入 TTL。
  - 请求结束后清理上下文。
  - `/api/management/current` 返回当前用户快照。
- 可写入简历程度：推荐写
- 原因：
  - 上下文设计和使用清楚。

### 文本清洗

- 是否已实现：部分实现
- 相关文件：
  - `main-application/src/main/java/com/azheng/boot/text/CleanTextUtil.java`
  - 类名：`CleanTextUtil`
  - 方法名：`cleanup`
- 核心逻辑：
  - 提供 BOM 移除、行尾空格清理、连续空行压缩、trim 等工具方法。
  - `rg` 未发现主链路调用 `CleanTextUtil`。
- 可写入简历程度：谨慎写
- 原因：
  - 工具类存在，但未接入文档入库主链路。

### 统一响应、异常处理、跨域

- 是否已实现：已实现
- 相关文件：
  - `core-framework/src/main/java/com/azheng/framework/web/Result.java`
  - `Results.java`
  - `GlobalExceptionHandler.java`
  - `BaseErrorCode.java`
  - `WebConfig.java`
- 核心逻辑：
  - `Result<T>` 统一 code/message/data。
  - `Results.success/failure` 构造统一响应。
  - `GlobalExceptionHandler` 捕获 `AbstractException`、`Throwable`、参数校验异常。
  - `WebConfig` 放开 CORS，并允许 credentials。
- 可写入简历程度：推荐写
- 原因：
  - 基础框架能力完整可见。

### 前端知识库管理页面

- 是否已实现：部分实现
- 相关文件：
  - `frontend/src/components/Admin.vue`
  - 方法名：`fetchKnowledgeList`、`loadDocuments`、`loadChunks`、`startChunking`、`saveChunkEdit`、`createChunk`、`handleDeleteConfirm`、`handleDocumentDeleteConfirm`、`handleChunkDeleteConfirm`
- 核心逻辑：
  - 调用后端 `/kb/pageQuery`、`/kb/createKb`、`/kb/renameKb/{id}`、`/kb/deleteKb/{id}`。
  - 调用 `/kb/doc/upload`、`/kb/doc/pageQuery`、`/kb/doc/startChunk`、`/kb/doc/deleteDoc/{id}`、`/kb/doc/switch`。
  - 调用 `/kb/doc/chunk/pageQuery`、`/kb/doc/chunk/mkdirText`、`/kb/doc/chunk/singleChunk`、`/kb/doc/chunk/deleteChunk/{id}`。
- 可写入简历程度：谨慎写
- 原因：
  - 接口调用完整；但当前 `Admin.vue` import 了不存在的 `DataOverview.vue`，并使用未定义的 `IntentManagement` 组件，前端构建风险较高。

### 前端 RAG 对话页面

- 是否已实现：已实现
- 相关文件：
  - `frontend/src/components/Home.vue`
  - 方法名：`sendMessage`、`regenerateResponse`、`cancelStreaming`、`fetchConversations`、`openConversation`、`deleteMessage`、`renameConversation`
- 核心逻辑：
  - 使用 `EventSource` 连接 `/agent/rag/streamingChat` 接收模型输出。
  - 监听 `init` 事件拿真实 conversationId，并替换临时会话。
  - 支持取消流式输出、加载历史会话、打开历史消息、删除消息、重命名会话。
- 可写入简历程度：推荐写
- 原因：
  - 聊天主流程前后端都可追踪。

## 4. RAG 主链路代码追踪

用户提问
→ 前端请求
- 文件路径：`frontend/src/components/Home.vue`
- 类名：Vue SFC
- 方法名：`sendMessage`
- 代码流程：将用户输入写入当前会话消息列表，生成 `roundId`，构造 `/agent/rag/streamingChat?prompt=...&roundId=...&openThinking=...`，已有会话时追加 `conversationId`，创建 `new EventSource(url)`。

→ Controller 接收
- 文件路径：`main-application/src/main/java/com/azheng/boot/rag/controller/RAGChatController.java`
- 类名：`RAGChatController`
- 方法名：`streamingChat`
- 代码流程：接收 `prompt`、`roundId`、可选 `conversationId`、`openThinking`，创建 `SseEmitter(3 * 60 * 1000L)`，调用 `ragChatService.streamingChat(...)`。

→ Service 处理
- 文件路径：`main-application/src/main/java/com/azheng/boot/rag/service/Impl/RAGChatServiceImpl.java`
- 类名：`RAGChatServiceImpl`
- 方法名：`streamingChat`
- 代码流程：获取当前用户、生成/复用 conversationId、构造流式模型、获取会话记忆、构造 LangChain4j `Query`。

→ 会话处理
- 文件路径：`RAGChatServiceImpl.java`
- 类名：`RAGChatServiceImpl`
- 方法名：`streamingChat`
- 代码流程：`UserContext.getUserId()` 获取用户；空 `conversationId` 时用 `IdUtil.getSnowflakeNextIdStr()` 生成会话；新会话通过 SSE `init` 事件返回 conversationId；通过 `originalChatMemoryProvider.get(actualConversationId)` 获取 Redis ChatMemory；通过 `chatMemory.messages()` 读取历史。
- 备注：代码中调用 `historyAbstractStore.query(historyMessages)`，传入的是消息列表而不是 `actualConversationId`，摘要读取逻辑疑似有误。

→ 查询处理
- 文件路径：`main-application/src/main/java/com/azheng/boot/rag/core/rewrite/QueryTransformerService.java`
- 类名：`QueryTransformerService`
- 方法名：`buildRewriteResult`
- 代码流程：先 `queryCompressRewrite(query)` 得到压缩后的问题，再 `queryExpandingRewrite(query)` 生成扩展子问题，返回 `RewriteResult`。

→ 意图识别
- 文件路径：`main-application/src/main/java/com/azheng/boot/rag/core/intent/IntentResolver.java`
- 类名：`IntentResolver`
- 方法名：`resolve`
- 代码流程：对子问题并行调用 `classifyIntents(q)`。
- 文件路径：`DefaultIntentClassifier.java`
- 类名：`DefaultIntentClassifier`
- 方法名：`classifyTargets`
- 代码流程：加载意图树叶子节点，渲染意图识别 prompt，调用 `chatModel.chat(ChatRequest)`，解析 LLM 返回 JSON 为 `NodeScore`。

→ 检索处理
- 文件路径：`main-application/src/main/java/com/azheng/boot/rag/core/retrieval/RetrievalEngine.java`
- 类名：`RetrievalEngine`
- 方法名：`retrieval`
- 代码流程：对子问题并行构建检索上下文，调用 `retrieveAndRerank()`。
- 文件路径：`MultiChannelRetrievalEngine.java`
- 类名：`MultiChannelRetrievalEngine`
- 方法名：`retrieveKnowledgeChannels`
- 代码流程：构造 `SearchContext`，执行启用的检索通道，再执行后处理器链。

→ 向量检索
- 文件路径：`IntentDirectedSearchChannel.java`
- 类名：`IntentDirectedSearchChannel`
- 方法名：`search`
- 代码流程：过滤 KB 意图，调用 `RecallService.recallMilvusContentWithKB()`。
- 文件路径：`VectorGlobalSearchChannel.java`
- 类名：`VectorGlobalSearchChannel`
- 方法名：`search`
- 代码流程：无意图或低置信时获取所有知识库 collection，调用 `RecallService.recallMilvusContentWithGlobal()`。
- 文件路径：`MilvusOperations.java`
- 类名：`MilvusOperations`
- 方法名：`search`
- 代码流程：问题 embedding 后构造 Milvus `SearchReq`，返回 `MilvusRetrievalResult`。

→ Rerank
- 文件路径：`RerankPostProcessor.java`
- 类名：`RerankPostProcessor`
- 方法名：`process`
- 代码流程：调用 `DashScopeRerankService.rerank(context.getMainQuestion(), chunks, context.getTopK())`。
- 文件路径：`DashScopeRerankService.java`
- 类名：`DashScopeRerankService`
- 方法名：`rerank`
- 代码流程：使用 DashScope `TextReRank` + `GTE_RERANK_V2` 对候选文本重排。

→ Prompt 组装
- 文件路径：`RAGPromptService.java`
- 类名：`RAGPromptService`
- 方法名：`buildStructuredMessages`
- 代码流程：调用 `buildSystemPrompt(context)` 选择模板，加入 `SystemMessage`；将 `context.getKbContext()` 包装为文档内容 UserMessage；再加入用户问题 UserMessage。
- 文件路径：`DefaultContextFormatter.java`
- 类名：`DefaultContextFormatter`
- 方法名：`formatKbContext`
- 代码流程：按意图组织 rerank 后 chunk，拼接知识库片段和意图规则。

→ 调用大模型
- 文件路径：`LLMConfig.java`
- 类名：`LLMConfig`
- 方法名：`buildQwenFlashStreamingModel`
- 代码流程：构造 `OpenAiStreamingChatModel`，modelName 为 `qwen-flash`，支持 `sendThinking(openThinking)`。
- 文件路径：`RAGChatServiceImpl.java`
- 类名：`RAGChatServiceImpl`
- 方法名：`LLMChatResponse`
- 代码流程：调用 `streamingChatModel.chat(messages, StreamingChatResponseHandler)`。

→ SSE 返回
- 文件路径：`RAGChatServiceImpl.java`
- 类名：`RAGChatServiceImpl`
- 方法名：`LLMChatResponse`
- 代码流程：`onPartialThinking` 和 `onPartialResponse` 中 `emitter.send(...)`；`onCompleteResponse` 中 `emitter.complete()`；错误时 `completeWithError()`。
- 文件路径：`frontend/src/components/Home.vue`
- 方法名：`sendMessage`
- 代码流程：`eventSource.onmessage` 接收分片并缓冲展示；`eventSource.addEventListener('init')` 获取 conversationId。

→ 消息保存
- 文件路径：`RAGChatServiceImpl.java`
- 类名：`RAGChatServiceImpl`
- 方法名：`treatResponse`
- 代码流程：模型完成后异步调用 `chatDataPersistenceService.mysqlPersistent(...)`，再执行摘要压缩、Redis 记忆追加、会话标题生成。
- 文件路径：`ChatDataPersistenceServiceImpl.java`
- 类名：`ChatDataPersistenceServiceImpl`
- 方法名：`mysqlPersistent`
- 代码流程：首轮插入 `conversation`，保存 user 和 ai 两条 `conversation_message`。
- 文件路径：`ConversationMessageServiceImpl.java`
- 类名：`ConversationMessageServiceImpl`
- 方法名：`saveConversationMessage`
- 代码流程：构造 `ConversationMessageDO` 并 insert。

## 5. 文档入库链路代码追踪

用户上传文档
→ 前端请求
- 文件路径：`frontend/src/components/Admin.vue`
- 类名：Vue SFC
- 方法名：`uploadFile`
- 代码流程：构造 `FormData`，追加 `file` 和 `kbId`，POST `/kb/doc/upload`。

→ 文件接收
- 文件路径：`main-application/src/main/java/com/azheng/boot/kb/controller/KnowledgeDocumentController.java`
- 类名：`KnowledgeDocumentController`
- 方法名：`upload`
- 代码流程：接收 `MultipartFile file` 和 `kbId`，调用 `knowledgeDocumentService.uploadFile(file, kbId)`。

→ 文件校验
- 文件路径：`FileValidationService.java`
- 类名：`FileValidationService`
- 方法名：`validateFile`
- 代码流程：校验空文件、50MB 大小限制、MIME/扩展名白名单、文件可读性、是否加密。

→ 文件存储
- 文件路径：`KnowledgeDocumentServiceImpl.java`
- 类名：`KnowledgeDocumentServiceImpl`
- 方法名：`uploadFile`
- 代码流程：生成 `uploadFile/{uuid}/{filename}` key，构造 `PutObjectRequest`，通过 `RustFsClient.putObject()` 写入 RustFS。
- 文件路径：`RustFSClientConfig.java`
- 类名：`RustFSClientConfig`
- 方法名：`RustFSClient`
- 代码流程：创建 S3Client，连接 S3 兼容 RustFS。

→ 保存文档元数据
- 文件路径：`KnowledgeDocumentServiceImpl.java`
- 类名：`KnowledgeDocumentServiceImpl`
- 方法名：`uploadFile`
- 代码流程：写入 `KnowledgeDocumentPO`，字段包括 documentName、fileType、fileSize、kbId、createBy；写入 `FileToRustFSLogPO(docId, rustfsKey)`。

→ 启动分块
- 文件路径：`frontend/src/components/Admin.vue`
- 方法名：`startChunking`
- 代码流程：POST `/kb/doc/startChunk`，请求体包含 `docId` 和 `chunkOptions.maxChunkSize/maxOverlapSize`。
- 文件路径：`KnowledgeDocumentController.java`
- 类名：`KnowledgeDocumentController`
- 方法名：`runChunk`
- 代码流程：调用 `knowledgeDocumentService.startChunking(startChunkRequest)`。
- 文件路径：`KnowledgeDocumentServiceImpl.java`
- 类名：`KnowledgeDocumentServiceImpl`
- 方法名：`startChunking`
- 代码流程：转调 `knowledgeChunkServiceImpl.performChunking(startChunkRequest)`。

→ 文本解析
- 文件路径：`knowledgeChunkServiceImpl.java`
- 类名：`knowledgeChunkServiceImpl`
- 方法名：`performChunking`
- 代码流程：按 docId 查询 `FileToRustFSLogPO` 获取 RustFS key，并调用 `langChain4jParser.TikaParser(rustfsKey)`。
- 文件路径：`LangChain4jParser.java`
- 类名：`LangChain4jParser`
- 方法名：`TikaParser`
- 代码流程：从 RustFS 读取文件流，使用 `ApacheTikaDocumentParser.parse(inputStream)` 生成 LangChain4j `Document`。

→ 文本清洗
- 当前代码未发现文档入库链路调用 `CleanTextUtil.cleanup()`。

→ 文本分块
- 文件路径：`LangChain4jSplitter.java`
- 类名：`LangChain4jSplitter`
- 方法名：`splitterToChunk`
- 代码流程：使用 `DocumentSplitters.recursive(maxChunkSize, maxOverlapSize)` 进行递归分块。

→ 向量化
- 文件路径：`VectorEmbedding.java`
- 类名：`VectorEmbedding`
- 方法名：`embed`
- 代码流程：对每个 `TextSegment` 调用 `embeddingModel.embed(textSegment)`，取得向量列表。
- 文件路径：`LLMConfig.java`
- 类名：`LLMConfig`
- 方法名：`defaultEmbeddingModel`
- 代码流程：构造 `OpenAiEmbeddingModel`，modelName 为 `text-embedding-v4`。

→ 写入向量库
- 文件路径：`VectorEmbedding.java`
- 类名：`VectorEmbedding`
- 方法名：`embed`
- 代码流程：构造 Milvus JSON 数据，字段包含 `chunk_text_vector`、`chunk_text_varchar`、`metadata`，调用 `milvusClientV2.insert()`。
- 文件路径：`MilvusOperations.java`
- 类名：`MilvusOperations`
- 方法名：`createChunkCollection`
- 代码流程：创建包含 `chunk_id`、`chunk_text_vector`、`chunk_text_varchar`、`metadata` 的 Milvus Collection，并建 AUTOINDEX/COSINE。

→ 保存 chunk 元数据
- 文件路径：`knowledgeChunkServiceImpl.java`
- 类名：`knowledgeChunkServiceImpl`
- 方法名：`performChunking`
- 代码流程：将每个 TextSegment 转为 `KnowledgeChunkPO`，保存 kbId、docId、milvusId、chunkIndex、chunkText、charCount、tokenCount、createBy，并批量 insert。

## 6. 候选简历亮点

### 亮点 1：企业知识库 RAG 应用完整链路

- 一句话描述：实现从知识库管理、文档入库、向量检索到 SSE 流式问答的企业知识助手原型。
- 代码依据：`KnowledgeBaseServiceImpl`、`knowledgeChunkServiceImpl`、`RAGChatServiceImpl`、`Home.vue`
- 技术含金量评分：8
- 面试风险：中
- 是否建议写入简历：推荐

### 亮点 2：文档入库流水线

- 一句话描述：基于 RustFS + Apache Tika + LangChain4j Splitter + Milvus 构建文档解析、分块、向量化入库链路。
- 代码依据：`KnowledgeDocumentServiceImpl.uploadFile`、`LangChain4jParser.TikaParser`、`LangChain4jSplitter.splitterToChunk`、`VectorEmbedding.embed`
- 技术含金量评分：8
- 面试风险：低
- 是否建议写入简历：推荐

### 亮点 3：Milvus Collection 与知识库绑定

- 一句话描述：创建知识库时同步创建独立 Milvus Collection，并在文档分块时按知识库 Collection 写入向量。
- 代码依据：`KnowledgeBaseServiceImpl.create`、`MilvusOperations.createChunkCollection`、`knowledgeChunkServiceImpl.performChunking`
- 技术含金量评分：7
- 面试风险：中
- 是否建议写入简历：推荐

### 亮点 4：Query Rewrite

- 一句话描述：通过 LangChain4j 的压缩改写和扩展改写生成更适合检索的问题和子问题。
- 代码依据：`QueryTransformerService.queryCompressRewrite`、`QueryTransformerService.queryExpandingRewrite`
- 技术含金量评分：7
- 面试风险：低
- 是否建议写入简历：推荐

### 亮点 5：LLM 意图树识别

- 一句话描述：将数据库维护的意图树渲染进 prompt，调用 LLM 输出意图分数，并结合 Redis 做意图树缓存。
- 代码依据：`DefaultIntentClassifier.classifyTargets`、`DefaultIntentClassifier.buildIntentPromptTemplate`、`IntentTreeCacheManager`
- 技术含金量评分：8
- 面试风险：中
- 是否建议写入简历：推荐

### 亮点 6：多通道召回框架

- 一句话描述：抽象 `SearchChannel`，支持意图定向检索和低置信全局向量兜底，并行执行后统一进入后处理链。
- 代码依据：`MultiChannelRetrievalEngine`、`IntentDirectedSearchChannel`、`VectorGlobalSearchChannel`
- 技术含金量评分：8
- 面试风险：中
- 是否建议写入简历：谨慎

### 亮点 7：Rerank 后处理链

- 一句话描述：通过后置处理器链接入 DashScope Rerank，对多路召回结果进行精排。
- 代码依据：`SearchResultPostProcessor`、`RerankPostProcessor`、`DashScopeRerankService`
- 技术含金量评分：7
- 面试风险：低
- 是否建议写入简历：推荐

### 亮点 8：SSE 流式问答与取消生成

- 一句话描述：后端使用 `SseEmitter` 输出模型分片，前端使用 `EventSource` 接收，并设计了基于 `roundId` 的取消接口。
- 代码依据：`RAGChatController.streamingChat`、`RAGChatServiceImpl.LLMChatResponse`、`RAGChatServiceImpl.cancel`、`Home.vue.cancelStreaming`
- 技术含金量评分：8
- 面试风险：中
- 是否建议写入简历：谨慎

### 亮点 9：Redis 会话记忆与自动摘要

- 一句话描述：使用 Redis 保存会话消息，并在对话轮次超过阈值时调用轻量模型生成历史摘要。
- 代码依据：`RedisChatMemoryStore`、`ConcurrentRedisChatMemory`、`AbstractCompressService`、`HistoryAbstractStore`
- 技术含金量评分：7
- 面试风险：高
- 是否建议写入简历：谨慎

### 亮点 10：统一 Web 基础框架

- 一句话描述：抽取统一响应、错误码、全局异常、MyBatis 分页、RedisTemplate、CORS 和用户上下文。
- 代码依据：`Result`、`Results`、`GlobalExceptionHandler`、`DataBaseConfiguration`、`WebConfig`、`UserContext`
- 技术含金量评分：6
- 面试风险：低
- 是否建议写入简历：推荐

## 7. 项目不足与可优化方向

- 权限控制不完整：
  - 代码依据：`KnowledgeBaseServiceImpl.pageQuery/delete/update`、`KnowledgeDocumentServiceImpl.docPageQuery/deleteFile` 未按当前用户过滤；`ConversationServiceImpl.deleteByConversationId/mkdirTitle/queryConversationMessage` 未完整校验 conversation 归属。
  - 结论：登录态已接入，但知识库级、文档级、会话操作级的用户隔离不完整。

- 密码安全不足：
  - 代码依据：`AuthServiceImpl.login` 使用 `user.getPassword().equals(password)`；`UserServiceImpl.createUser/update/changePassword` 直接保存新密码。
  - 结论：当前代码未发现 BCrypt/哈希校验；`resources/database/init_data.sql` 中 admin 密码看起来是 BCrypt 字符串，但登录代码不做 BCrypt match。

- RAG 所需的若干 Bean 未发现定义：
  - 代码依据：`RAGChatServiceImpl` 注入 `streamHandleCache`、`treatResponseThreadPoolExecutor`；`IntentResolver` 注入 `intentClassifyThreadPoolExecutor`；`RetrievalEngine` 注入 `ragContextThreadPoolExecutor`；`MultiChannelRetrievalEngine` 注入 `ragRetrievalThreadPoolExecutor`；`RecallService` 注入 `ragInnerRetrievalThreadPoolExecutor`。
  - 结论：当前代码未发现这些 named Bean 的 `@Bean` 定义，应用启动可能失败；代码中未确认可运行。

- 检索策略仍偏单一：
  - 代码依据：已实现 `IntentDirectedSearchChannel` 和 `VectorGlobalSearchChannel`；`SearchChannelType.KEYWORD_ES` 只有枚举，未发现 ES/BM25 实现。
  - 结论：可以说多通道向量召回，不建议说已实现完整混合检索。

- TopK multiplier 使用可能有误：
  - 代码依据：`IntentDirectedSearchChannel.search` 读取 `topKMultiplier` 后直接作为 Milvus TopK；`VectorGlobalSearchChannel.search` 读取的是 `intentDirected.topKMultiplier` 而不是 `vectorGlobal.topKMultiplier`。
  - 结论：配置名表达的是倍数，但代码没有 `context.topK * multiplier`；全局通道还疑似取错配置项。

- RAG 多轮上下文接入不完整：
  - 代码依据：`RAGPromptService.buildStructuredMessages` 接收 `history`、`subQuestions` 参数但未加入最终 messages；`RAGChatServiceImpl.streamingChat` 查询摘要时传 `historyMessages` 给 `historyAbstractStore.query()`。
  - 结论：Redis 记忆和摘要组件存在，但主 RAG prompt 中未确认完整使用历史上下文。

- 检索为空时未持久化本轮对话：
  - 代码依据：`RAGChatServiceImpl.streamingChat` 中 `retrievalContext.isEmpty()` 分支直接 `emitter.send()`、`emitter.complete()`、`return`，未调用 `treatResponse()` 或 MySQL 持久化。
  - 结论：空检索回复可能不会进入历史会话。

- 文档分块缺少异步任务状态管理：
  - 代码依据：`KnowledgeDocumentServiceImpl.startChunking` 直接同步调用 `knowledgeChunkServiceImpl.performChunking`；`knowledgeChunkServiceImpl.performChunking` 注释写有 TODO 改异步。
  - 结论：当前未发现任务表、进度状态、失败重试、断点续跑。

- 删除链路存在数据一致性缺口：
  - 代码依据：`KnowledgeBaseServiceImpl.delete` 未删除 Milvus Collection；`KnowledgeDocumentServiceImpl.deleteFile` 未删除 RustFS 对象和 Milvus 向量；`knowledgeChunkServiceImpl.remove` 中 Milvus 删除被注释。
  - 结论：当前主要是 MySQL 逻辑删除，不是全资源清理。

- 重复分块缺少幂等控制：
  - 代码依据：`KnowledgeDocumentController.runChunk` 到 `knowledgeChunkServiceImpl.performChunking` 未发现对同 docId 已有 chunk 的去重或清理。
  - 结论：重复启动分块可能重复写入 Milvus 和 MySQL。

- 文本清洗未接入入库链路：
  - 代码依据：`CleanTextUtil` 存在，但 `rg` 只发现其定义，未发现被 `performChunking` 或 parser 调用。
  - 结论：不能写已实现完整清洗流水线。

- 缺少评测体系：
  - 代码依据：当前未发现 RAG 测评集、召回率/命中率评估、答案质量评测、离线评估脚本或表结构。
  - 结论：代码中未确认。

- 缺少监控与链路追踪：
  - 代码依据：仅发现 `log.info/warn/error`，未发现 Micrometer、Prometheus、OpenTelemetry、TraceId 传递等。
  - 结论：代码中未确认生产级监控。

- 缺少接口文档：
  - 代码依据：未发现 OpenAPI/Swagger/Springdoc 依赖和配置。
  - 结论：代码中未确认。

- 前端存在构建风险：
  - 代码依据：`Admin.vue` import `./DataOverview.vue`，但 `frontend/src/components/DataOverview.vue` 不存在；模板使用 `<IntentManagement />`，未发现导入或定义。
  - 结论：前端管理页当前代码可能无法通过构建，需补齐或移除。

- 前后端登录态处理不一致：
  - 代码依据：`main.js` axios interceptor 从 `localStorage.token` 取 token 并写 `Authorization: Bearer ...`；`Login.vue` 登录成功后当前未保存 token；后端 Sa-Token 配置 token-name 为 `satoken`。
  - 结论：可能依赖 Sa-Token Cookie，但前端本地 token 逻辑和登录代码不一致；具体运行方式代码中未完全确认。

- 配置硬编码较多：
  - 代码依据：`RustFSClientConfig.RustFSClient()` 硬编码 `http://192.168.191.128:9001`、`rustfsadmin`；`application.yaml` 也包含固定 MySQL/Redis/Milvus IP 和密码。
  - 结论：部署灵活性和安全性不足。

- 未发现性能压测：
  - 代码依据：当前未发现 JMeter/Gatling/k6/压测脚本或报告。
  - 结论：代码中未确认。

