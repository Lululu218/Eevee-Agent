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

package com.azheng.boot.rag.core.intent;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.azheng.boot.rag.core.prompt.PromptTemplateLoader;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.*;
import com.azheng.boot.rag.dao.entity.IntentNodeDO;
import com.azheng.boot.rag.dao.mapper.IntentNodeMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azheng.boot.rag.constant.RAGConstant.INTENT_PROMPT_TEMPLATE_PATH;
import static com.azheng.boot.rag.constant.RAGConstant.TITLE_SUMMARY_TEMPLATE_PATH;

/**
 * LLM 树形意图分类器（串行实现）
 * <p>
 * 将所有意图节点一次性发送给 LLM 进行识别打分，适用于意图数量较少的场景
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultIntentClassifier implements IntentClassifier, IntentNodeRegistry {

    @Resource
    private final IntentNodeMapper intentNodeMapper;

    @Resource
    private final IntentTreeCacheManager intentTreeCacheManager;

    @Resource
    private ChatModel chatModel;

    @Resource
    private PromptTemplateLoader promptTemplateLoader;

    @PostConstruct
    public void init() {
        // 初始化时确保Redis缓存存在
        ensureIntentTreeCached();
        log.info("意图分类器初始化完成");
    }

    /**
     * 确保Redis缓存中有意图树数据
     * 如果缓存不存在，从数据库加载并保存到Redis
     */
    private void ensureIntentTreeCached() {
        if (!intentTreeCacheManager.isCacheExists()) {
            List<IntentNode> roots = loadIntentTreeFromDB();
            if (!roots.isEmpty()) {
                intentTreeCacheManager.saveIntentTreeToCache(roots);
                log.info("意图树已从数据库加载并缓存到Redis");
            }
        }
    }

    /**
     * 从Redis加载意图树并构建内存结构
     * 每次调用都会重新从Redis读取，确保数据是最新的
     */
    private IntentTreeData loadIntentTreeData() {
        // 1. 从Redis读取（如果不存在会自动从数据库加载）
        List<IntentNode> roots = intentTreeCacheManager.getIntentTreeFromCache();

        // 2. 如果Redis也没有，从数据库加载并缓存
        if (CollUtil.isEmpty(roots)) {
            roots = loadIntentTreeFromDB();
            if (!roots.isEmpty()) {
                intentTreeCacheManager.saveIntentTreeToCache(roots);
            }
        }

        // 3. 构建内存结构（临时使用）
        if (CollUtil.isEmpty(roots)) {
            return new IntentTreeData(List.of(), List.of(), Map.of());
        }

        List<IntentNode> allNodes = flatten(roots);
        List<IntentNode> leafNodes = allNodes.stream()
                .filter(IntentNode::isLeaf)
                .collect(Collectors.toList());
        Map<String, IntentNode> id2Node = allNodes.stream()
                .collect(Collectors.toMap(IntentNode::getId, n -> n));

        log.debug("意图树数据加载完成, 总节点数: {}, 叶子节点数: {}", allNodes.size(), leafNodes.size());

        return new IntentTreeData(allNodes, leafNodes, id2Node);
    }

    @Override
    public IntentNode getNodeById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        IntentTreeData data = loadIntentTreeData();
        return data.id2Node.get(id);
    }

    /**
     * 意图树数据结构（临时对象，不持久化）
     */
    private record IntentTreeData(
            // 全部节点
            List<IntentNode> allNodes,
            // 叶子节点
            List<IntentNode> leafNodes,
            // id-节点集合
            Map<String, IntentNode> id2Node
    ) {
    }

    private List<IntentNode> flatten(List<IntentNode> roots) {
        List<IntentNode> result = new ArrayList<>();
        Deque<IntentNode> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            IntentNode n = stack.pop();
            result.add(n);
            if (n.getChildren() != null) {
                for (IntentNode child : n.getChildren()) {
                    stack.push(child);
                }
            }
        }
        return result;
    }

    /**
     * 对所有"叶子分类节点"做意图识别，由 LLM 输出每个分类的 score
     * - 返回结果已按 score 从高到低排序
     */
    @Override
    public List<NodeScore> classifyTargets(String question) {
        // 每次都从Redis读取最新数据
        IntentTreeData data = loadIntentTreeData();

        // 1.直接调用buildPrompt，传入当前意图树缓存
        String template = buildIntentPromptTemplate(data.leafNodes);

        // 2.构建当前场景的ChatRequest：消息列表（用户提问+意图识别提示词模版）
        ChatRequest chatRequest = ChatRequest
                .builder()
                .messages(List.of(
                        new SystemMessage(template),
                        new UserMessage(question)
                ))
                .temperature(0.1D)
                .topP(0.3D)
                .build();

        // 3.调用模型，获取模型输出的提示词模版指定的输出格式内容
        ChatResponse chatResponse = chatModel.chat(chatRequest);
        String responseText = chatResponse.aiMessage().text();

        try {
            // 4.修复可能被截断的JSON（流式输出截断是常见问题）
            String fixedJson = fixTruncatedJson(responseText);
            if (fixedJson == null) {
                log.warn("[INTENT] LLM 返回的 JSON 无法修复, 原始响应: {}", truncateString(responseText, 500));
                return List.of();
            }

            // 5.解析输出（设定的是Json格式的字符串）
            JsonElement jsonElement = JsonParser.parseString(fixedJson);

            // 6.拦截检验模型输出格式
            JsonArray arr;
            if (jsonElement.isJsonArray()) {
                arr = jsonElement.getAsJsonArray();
            } else if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("results")) {
                // 容错：如果模型外面又包了一层 { "results": [...] }
                arr = jsonElement.getAsJsonObject().getAsJsonArray("results");
            } else {
                log.warn("LLM 返回了非预期的 JSON 格式, 原始响应: {}", chatResponse.aiMessage().text());
                return List.of();
            }

            // 7.循环封装NodeScore
            List<NodeScore> nodeScores = new ArrayList<>();
            for (JsonElement element : arr) {
                // 第一步：不是jsonObjet对象？下一个：获取
                if (!element.isJsonObject()) continue;
                JsonObject jsonObject = element.getAsJsonObject();

                // 第二步：jsonObjet对象没有“id”、“score”? 下一个 : 获取
//                if (!jsonObject.has("id") || jsonObject.has("score")) continue;
                String id = jsonObject.get("id").getAsString();
                double score = jsonObject.get("score").getAsDouble();

                //根据id获取node
                IntentNode intentNode = data.id2Node.get(id);
                if (intentNode == null) {
                    log.warn("LLM 返回了未知的意图节点 ID: {}, 已跳过", id);
                    continue;
                }
                nodeScores.add(new NodeScore(intentNode, score));
            }

            // 8.按分数降序(reversed)排序List<NodeScore> scores
            nodeScores.sort(Comparator.comparingDouble(NodeScore::getScore).reversed());

            // 9.打印日志
            log.info("当前问题：{}\n意图识别树如下所示：{}\n",
                    question,
                    JSONUtil.toJsonPrettyStr(
                            nodeScores.stream().peek(each -> {
                                IntentNode node = each.getNode();
                                node.setChildren(null);
                            }).collect(Collectors.toList())
                    )
            );

            // 10.返回scores
            return nodeScores;
        } catch (Exception e) {
            log.warn("解析LLM响应失败，原始问题：{}",responseText, e);
            return List.of();
        }
    }

    /**
     * 渲染意图识别提示词模版
     * {intent_list}
     * @param nodes 意图树的所有节点
     * @return String 渲染后的模版
     */
    public String buildIntentPromptTemplate(List<IntentNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (IntentNode node : nodes) {
            /**
             * id：节点id
             * path：节点所在路径（如 IT > 部门A > 项目经理信息）
             * 描述：节点的语义描述
             */
            // 意图节点基础信息：
            sb.append("- id=").append(node.getId()).append("\n");
            sb.append("  path=").append(node.getFullPath()).append("\n");
            sb.append("  description=").append(node.getDescription()).append("\n");

            // 节点类型
            if(node.isKB()){
                sb.append("  type=KB\n");
            } else if (node.isSystem()) {
                sb.append("  type=System\n");
            }

            // 示例问题
            if (node.getExamples() != null && !node.getExamples().isEmpty()) {
                sb.append("  examples=");
                sb.append(String.join(" / ", node.getExamples()));
                sb.append("\n");
            }
            sb.append("\n");
        }
        return promptTemplateLoader.render(
                INTENT_PROMPT_TEMPLATE_PATH,
                Map.of("intent_list",sb.toString())
        );
    }

    /**
     * 方便使用：
     * - 只取前 topN
     * - 过滤掉 score < minScore 的分类
     */
    @Override
    public List<NodeScore> topKAboveThreshold(String question, int topN, double minScore) {
        return classifyTargets(question).stream()
                .filter(ns -> ns.getScore() >= minScore)
                .limit(topN)
                .toList();
    }

    private List<IntentNode> loadIntentTreeFromDB() {
        // 1. 查出所有未删除节点（扁平结构）
        List<IntentNodeDO> intentNodeDOList = intentNodeMapper.selectList(
                Wrappers.lambdaQuery(IntentNodeDO.class)
                        .eq(IntentNodeDO::getDelFlag, 0)
        );

        if (intentNodeDOList.isEmpty()) {
            return List.of();
        }

        // 2. DO -> IntentNode（第一遍：先把所有节点建出来，放到 map 里）
        Map<String, IntentNode> id2Node = new HashMap<>();
        for (IntentNodeDO each : intentNodeDOList) {
            IntentNode node = BeanUtil.toBean(each, IntentNode.class);
            // 数据库中的 code 映射到 IntentNode 的 id/parentId
            node.setId(each.getIntentCode());
            node.setParentId(each.getParentCode());
            // 确保 children 不为 null（避免后面 add NPE）
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
            id2Node.put(node.getId(), node);
        }

        // 3. 第二遍：根据 parentId 组装 parent -> children
        List<IntentNode> roots = new ArrayList<>();
        for (IntentNode node : id2Node.values()) {
            String parentId = node.getParentId();
            if (parentId == null || parentId.isBlank()) {
                // 没有 parentId，当作根节点
                roots.add(node);
                continue;
            }

            IntentNode parent = id2Node.get(parentId);
            if (parent == null) {
                // 找不到父节点，兜底也当作根节点，避免节点丢失
                roots.add(node);
                continue;
            }

            // 追加到父节点的 children
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(node);
        }

        // 4. 填充 fullPath（跟你原来的 fillFullPath 一样的逻辑）
        fillFullPath(roots, null);

        return roots;
    }

    /**
     * 填充 fullPath 字段，效果类似：
     * - 集团信息化
     * - 集团信息化 > 人事
     * - 业务系统 > OA系统 > 系统介绍
     */
    private void fillFullPath(List<IntentNode> nodes, IntentNode parent) {
        if (nodes == null) return;

        for (IntentNode node : nodes) {
            if (parent == null) {
                node.setFullPath(node.getName());
            } else {
                node.setFullPath(parent.getFullPath() + " > " + node.getName());
            }

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                fillFullPath(node.getChildren(), node);
            }
        }
    }

    /**
     * 修复可能被截断的JSON字符串
     * 处理流式输出截断导致的JSON不完整问题
     * @param input 原始JSON字符串
     * @return 修复后的JSON字符串，无法修复返回null
     */
    private String fixTruncatedJson(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        // 1. 清理前后空白和markdown代码块标记
        String cleaned = input.trim();
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");

        // 2. 找到JSON的实际开始和结束位置
        int startIdx = cleaned.indexOf('[');
        int endIdx = cleaned.lastIndexOf(']');

        if (startIdx == -1) {
            // 尝试找对象格式
            startIdx = cleaned.indexOf('{');
            endIdx = cleaned.lastIndexOf('}');
        }

        if (startIdx == -1 || endIdx == -1 || startIdx > endIdx) {
            log.warn("[INTENT] 无法找到有效的JSON结构, 输入: {}", truncateString(input, 200));
            return null;
        }

        // 3. 提取JSON部分
        String jsonPart = cleaned.substring(startIdx, endIdx + 1);

        // 4. 修复未闭合的字符串（最常见的截断问题）
        jsonPart = fixUnterminatedStrings(jsonPart);

        // 5. 验证修复结果
        if (isValidJson(jsonPart)) {
            return jsonPart;
        }

        // 6. 尝试进一步修复
        String furtherFixed = tryFixJson(jsonPart);
        if (furtherFixed != null && isValidJson(furtherFixed)) {
            return furtherFixed;
        }

        log.warn("[INTENT] JSON修复失败, 原始: {}", truncateString(input, 200));
        return null;
    }

    /**
     * 修复未闭合的字符串
     * 处理 "unterminated string" 问题
     */
    private String fixUnterminatedStrings(String json) {
        if (json == null) return null;

        StringBuilder result = new StringBuilder(json);
        int balance = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);

            if (escape) {
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == '"' && !escape) {
                inString = !inString;
                continue;
            }

            // 在字符串外统计花括号和方括号的平衡
            if (!inString) {
                if (c == '{' || c == '[') {
                    balance++;
                } else if (c == '}' || c == ']') {
                    balance--;
                }
            }
        }

        // 如果字符串未闭合，尝试修复
        if (inString) {
            // 找到最后一个未闭合的引号位置
            int lastQuote = result.lastIndexOf("\"");
            if (lastQuote != -1) {
                // 从最后一个引号之后查找可能的闭合位置
                String remaining = result.substring(lastQuote + 1);
                // 查找下一个引号或结构结束符
                int nextQuote = remaining.indexOf("\"");
                int nextBracket = remaining.indexOf(']');
                int nextBrace = remaining.indexOf('}');

                // 如果没有找到合适的闭合位置，在末尾添加引号
                if (nextQuote == -1 || (nextBracket != -1 && nextBracket < nextQuote) || 
                    (nextBrace != -1 && nextBrace < nextQuote)) {
                    // 在适当位置添加闭合引号
                    int closePos = result.length();
                    if (nextBracket != -1) closePos = Math.min(closePos, lastQuote + 1 + nextBracket);
                    if (nextBrace != -1) closePos = Math.min(closePos, lastQuote + 1 + nextBrace);
                    result.insert(closePos, "\"");
                    log.debug("[INTENT] 修复未闭合的字符串，在位置 {} 添加了引号", closePos);
                }
            }
        }

        // 修复不平衡的括号
        while (balance > 0) {
            // 检查最后一个未闭合的开括号类型
            int lastOpenBrace = result.lastIndexOf("{");
            int lastOpenBracket = result.lastIndexOf("[");
            
            if (lastOpenBrace > lastOpenBracket) {
                result.append("}");
            } else {
                result.append("]");
            }
            balance--;
            log.debug("[INTENT] 添加闭合括号");
        }

        return result.toString();
    }

    /**
     * 尝试多种方式修复JSON
     */
    private String tryFixJson(String json) {
        // 尝试移除末尾不完整的内容
        String[] patterns = {
            "(,\\s*[^\\]}\\s]*)$",  // 末尾不完整的元素
            "(\"[^\"]*)$",           // 末尾不完整的字符串
            "(,\\s*)$"               // 末尾多余的逗号
        };

        for (String pattern : patterns) {
            String fixed = json.replaceAll(pattern, "");
            if (isValidJson(fixed)) {
                log.debug("[INTENT] 通过模式 {} 修复了JSON", pattern);
                return fixed;
            }
        }

        // 尝试添加缺失的闭合括号
        if (!json.endsWith("]") && json.startsWith("[")) {
            String fixed = json + "]";
            if (isValidJson(fixed)) {
                log.debug("[INTENT] 添加了缺失的 ]");
                return fixed;
            }
        }

        if (!json.endsWith("}") && json.startsWith("{")) {
            String fixed = json + "}";
            if (isValidJson(fixed)) {
                log.debug("[INTENT] 添加了缺失的 }");
                return fixed;
            }
        }

        return null;
    }

    /**
     * 验证JSON是否有效
     */
    private boolean isValidJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 截断字符串用于日志输出
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }
}