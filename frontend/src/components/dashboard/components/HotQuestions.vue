
<template>
  <div class="hot-questions">
    <div class="section-header">
      <h3 class="section-title">{{ title }}</h3>
      <button class="refresh-btn" @click="$emit('refresh')">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21.5 2v6h-6M2.5 22v-6h6"/><path d="M2 12a10 10 0 0 1 18.8-4.3"/><path d="M22 12a10 10 0 0 1-18.8 4.2"/>
        </svg>
      </button>
    </div>
    <div class="questions-list">
      <div 
        v-for="(item, index) in questions" 
        :key="item.id" 
        class="question-item"
      >
        <div class="question-rank" :class="getRankClass(index)">
          {{ index + 1 }}
        </div>
        <div class="question-content">
          <p class="question-text">{{ item.question }}</p>
          <div class="question-meta">
            <span class="question-count">提问 {{ item.count }} 次</span>
            <span class="question-growth" :class="item.growthRate >= 0 ? 'positive' : 'negative'">
              {{ item.growthRate >= 0 ? '+' : '' }}{{ item.growthRate.toFixed(1) }}%
            </span>
          </div>
        </div>
        <div class="question-bar-container">
          <div class="question-bar" :style="{ width: item.percentage + '%' }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '热点问题'
  },
  questions: {
    type: Array,
    default: () => []
  }
})

defineEmits(['refresh'])

const getRankClass = (index) => {
  switch (index) {
    case 0: return 'rank-top'
    case 1: return 'rank-second'
    case 2: return 'rank-third'
    default: return 'rank-other'
  }
}
</script>

<style scoped>
.hot-questions {
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
}

.refresh-btn {
  padding: 6px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn:hover {
  background: #e5e7eb;
  color: #667eea;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

.questions-list::-webkit-scrollbar {
  width: 6px;
}

.questions-list::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 3px;
}

.questions-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.questions-list::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.question-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.25s ease;
}

.question-item:hover {
  background: #f3f4f6;
}

.question-rank {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #86909C;
  background: #e5e7eb;
  flex-shrink: 0;
}

.rank-top {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
}

.rank-second {
  background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
  color: white;
}

.rank-third {
  background: linear-gradient(135deg, #CD7F32, #B87333);
  color: white;
}

.question-content {
  flex: 1;
  min-width: 0;
}

.question-text {
  font-size: 14px;
  font-weight: 500;
  color: #1D2129;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.question-count {
  font-size: 12px;
  color: #86909C;
}

.question-growth {
  font-size: 12px;
  font-weight: 500;
}

.question-growth.positive {
  color: #00B42A;
}

.question-growth.negative {
  color: #F53F3F;
}

.question-bar-container {
  width: 80px;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}

.question-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #36BFFA);
  border-radius: 3px;
  transition: width 0.5s ease;
}
</style>
