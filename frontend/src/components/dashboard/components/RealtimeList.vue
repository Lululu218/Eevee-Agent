
<template>
  <div class="realtime-list">
    <div class="list-header">
      <div class="header-left">
        <h3 class="list-title">{{ title }}</h3>
        <span class="live-indicator">
          <span class="live-dot"></span>
          实时
        </span>
      </div>
      <div class="header-right">
        <select v-model="sortBy" class="sort-select">
          <option value="time">按时间</option>
          <option value="responseTime">按响应时间</option>
        </select>
      </div>
    </div>
    <div class="list-container">
      <div 
        v-for="item in sortedItems" 
        :key="item.eventId || item.id" 
        class="list-item"
      >
        <div class="item-avatar">
          <span class="avatar-text">{{ item.userName?.charAt(0) || '?' }}</span>
        </div>
        <div class="item-content">
          <div class="item-header">
            <span class="user-name">{{ item.userName || '未知用户' }}</span>
            <span class="item-time">{{ formatTime(item.eventTime) }}</span>
          </div>
          <p class="item-message">{{ item.messageContent }}</p>
          <div class="item-meta">
            <span class="meta-tag" :class="getSourceClass(item.sourceType)">
              {{ getSourceText(item.sourceType) }}
            </span>
            <span v-if="item.responseTime" class="response-time">
              ⏱ {{ item.responseTime }}ms
            </span>
          </div>
        </div>
        <div class="item-status">
          <span class="status-dot" :class="item.status?.toLowerCase()"></span>
        </div>
      </div>
      <div v-if="items.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无数据</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '实时会话'
  },
  items: {
    type: Array,
    default: () => []
  }
})

const sortBy = ref('time')

const sortedItems = computed(() => {
  const items = [...props.items]
  if (sortBy.value === 'time') {
    return items.sort((a, b) => {
      const timeA = new Date(a.eventTime).getTime()
      const timeB = new Date(b.eventTime).getTime()
      return timeB - timeA
    })
  } else {
    return items.sort((a, b) => {
      return (a.responseTime || 0) - (b.responseTime || 0)
    })
  }
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

const getSourceClass = (source) => {
  switch (source) {
    case 'DIRECT': return 'tag-direct'
    case 'KNOWLEDGE_BASE': return 'tag-knowledge'
    case 'TOOL': return 'tag-tool'
    default: return 'tag-other'
  }
}

const getSourceText = (source) => {
  switch (source) {
    case 'DIRECT': return '直接提问'
    case 'KNOWLEDGE_BASE': return '知识库'
    case 'TOOL': return '工具调用'
    default: return '其他'
  }
}
</script>

<style scoped>
.realtime-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #00B42A;
  font-weight: 500;
  padding: 4px 10px;
  background: rgba(0, 180, 42, 0.1);
  border-radius: 12px;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #00B42A;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.sort-select {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 12px;
  color: #4b5563;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.25s ease;
}

.sort-select:hover {
  border-color: #667eea;
}

.sort-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.list-container {
  flex: 1;
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-container::-webkit-scrollbar {
  width: 6px;
}

.list-container::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 3px;
}

.list-container::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.list-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.25s ease;
}

.list-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.item-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #36BFFA);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.item-time {
  font-size: 12px;
  color: #86909C;
}

.item-message {
  font-size: 13px;
  color: #4b5563;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.meta-tag {
  font-size: 11px;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 6px;
}

.tag-direct {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.tag-knowledge {
  color: #00B42A;
  background: rgba(0, 180, 42, 0.1);
}

.tag-tool {
  color: #FFB700;
  background: rgba(255, 183, 0, 0.1);
}

.tag-other {
  color: #86909C;
  background: rgba(134, 144, 156, 0.1);
}

.response-time {
  font-size: 11px;
  color: #86909C;
}

.item-status {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  padding-top: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active {
  background: #00B42A;
  box-shadow: 0 0 0 3px rgba(0, 180, 42, 0.2);
}

.status-dot.ended {
  background: #86909C;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #86909C;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}
</style>
