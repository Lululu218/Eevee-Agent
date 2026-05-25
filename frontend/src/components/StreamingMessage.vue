<template>
  <div class="streaming-message" :class="{ 'is-streaming': isStreaming, 'is-thinking': isThinking }">
    <!-- 头像 -->
    <div class="avatar">
      <img src="https://neeko-copilot.bytedance.net/api/text_to_image?prompt=cute%20fox%20avatar%20cartoon%20style&image_size=square" alt="AI" />
    </div>
    
    <!-- 消息内容 -->
    <div class="message-content">
      <div class="message-header">
        <span class="sender">EeveeAgent</span>
        <span class="status-indicator" v-if="isStreaming">
          <span class="pulse-dot"></span>
          <span class="pulse-dot"></span>
          <span class="pulse-dot"></span>
        </span>
      </div>
      
      <div class="message-body">
        <!-- 思考状态 -->
        <div v-if="isThinking && !isStreaming" class="thinking-content">
          <span class="thinking-icon">🤔</span>
          <span class="thinking-text">正在思考中...</span>
        </div>
        
        <!-- 流式内容 -->
        <div v-else class="content-wrapper">
          <!-- 检测代码块 -->
          <template v-if="hasCodeBlock">
            <div v-for="(block, index) in codeBlocks" :key="index">
              <!-- 代码块之前的文本 -->
              <div v-if="block.beforeText" class="text-content" v-html="renderMarkdown(block.beforeText)"></div>
              <!-- 代码块 -->
              <CodeHighlighter :content="block.code" :language="block.language" />
            </div>
            <!-- 最后一个代码块之后的文本 -->
            <div v-if="remainingText" class="text-content" v-html="renderMarkdown(remainingText)"></div>
          </template>
          
          <!-- 普通文本 -->
          <div v-else class="text-content" v-html="renderMarkdown(displayContent)"></div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="message-actions" v-if="showActions">
        <button class="action-btn" @click="$emit('cancel')" v-if="isStreaming">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="9" y1="9" x2="15" y2="15"></line>
            <line x1="15" y1="9" x2="9" y2="15"></line>
          </svg>
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>import { ref, computed, watch } from 'vue';
import CodeHighlighter from './CodeHighlighter.vue';
const props = defineProps({
 message: {
 type: Object,
 required: true
 }
});
defineEmits(['cancel']);
// 从消息中提取状态
const isThinking = computed(() => props.message.isThinking);
const isStreaming = computed(() => props.message.isStreaming);
const displayContent = computed(() => props.message.content || '');
const showActions = computed(() => isStreaming.value);
// 代码块正则
const CODE_BLOCK_REGEX = /```(\w+)?\n([\s\S]*?)```/g;
// 检测是否包含代码块
const hasCodeBlock = computed(() => {
 return CODE_BLOCK_REGEX.test(displayContent.value);
});
// 解析代码块
const codeBlocks = computed(() => {
 const content = displayContent.value;
 const blocks = [];
 let lastIndex = 0;
 let match;
 // 重置正则
 CODE_BLOCK_REGEX.lastIndex = 0;
 while ((match = CODE_BLOCK_REGEX.exec(content)) !== null) {
 // 代码块之前的文本
 if (match.index > lastIndex) {
 blocks.push({
 beforeText: content.substring(lastIndex, match.index),
 code: '',
 language: ''
 });
 }
 // 代码块
 blocks.push({
 beforeText: '',
 code: match[2] || '',
 language: match[1] || 'plaintext'
 });
 lastIndex = match.index + match[0].length;
 }
 return blocks;
});
// 剩余文本（最后一个代码块之后）
const remainingText = computed(() => {
 const content = displayContent.value;
 CODE_BLOCK_REGEX.lastIndex = 0;
 let lastMatch;
 let match;
 while ((match = CODE_BLOCK_REGEX.exec(content)) !== null) {
 lastMatch = match;
 }
 if (lastMatch) {
 const endIndex = lastMatch.index + lastMatch[0].length;
 return content.substring(endIndex);
 }
 return '';
});
// 简单的Markdown渲染
const renderMarkdown = (text) => {
 if (!text)
 return '';
 let result = text;
 // 粗体
 result = result.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
 result = result.replace(/__(.+?)__/g, '<strong>$1</strong>');
 // 斜体
 result = result.replace(/\*(.+?)\*/g, '<em>$1</em>');
 result = result.replace(/_(.+?)_/g, '<em>$1</em>');
 // 链接
 result = result.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');
 // 换行
 result = result.replace(/\n/g, '<br/>');
 // 列表
 result = result.replace(/^\s*-\s(.+)$/gm, '<li>$1</li>');
 result = result.replace(/(<li>.+<\/li>)+/g, '<ul>$&</ul>');
 return result;
};
</script>

<style scoped>
.streaming-message {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-bottom: 12px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.2);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.sender {
  font-weight: 600;
  color: #fff;
  font-size: 14px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  animation: pulse 1.4s ease-in-out infinite;
}

.pulse-dot:nth-child(1) {
  animation-delay: 0s;
}

.pulse-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.pulse-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

.message-body {
  color: rgba(255, 255, 255, 0.95);
  font-size: 15px;
  line-height: 1.7;
}

.thinking-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.thinking-icon {
  font-size: 20px;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.thinking-text {
  font-style: italic;
  opacity: 0.8;
}

.content-wrapper {
  word-break: break-word;
}

.text-content {
  margin: 4px 0;
}

.text-content :deep(a) {
  color: #ffd700;
  text-decoration: underline;
}

.text-content :deep(strong) {
  font-weight: 700;
}

.text-content :deep(em) {
  font-style: italic;
}

.text-content :deep(ul) {
  padding-left: 20px;
  margin: 8px 0;
}

.text-content :deep(li) {
  margin: 4px 0;
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

/* 滚动条样式 */
.message-body::-webkit-scrollbar {
  width: 6px;
}

.message-body::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.message-body::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.message-body::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
