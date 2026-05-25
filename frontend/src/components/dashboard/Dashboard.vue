
<template>
  <div class="dashboard">
    <!-- 顶部控制栏 -->
    <div class="dashboard-header">
      <!-- 大标题 -->
      <h2 class="dashboard-title">DashBoard</h2>
      
      <!-- 时间选择和操作区域 -->
      <div class="time-select-wrapper">
        <!-- 时间范围选择器 -->
        <div class="time-range-selector">
          <button 
            v-for="range in timeRanges" 
            :key="range.value"
            :class="['range-btn', { active: selectedRange === range.value }]"
            @click="selectTimeRange(range.value)"
          >
            {{ range.label }}
          </button>
        </div>
        
        <!-- 分隔线 -->
        <div class="divider"></div>
        
        <!-- 刷新按钮 -->
        <button class="refresh-btn" :class="{ refreshing: isRefreshing }" @click="refreshData">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21.5 2v6h-6M2.5 22v-6h6"/><path d="M2 12a10 10 0 0 1 18.8-4.3"/><path d="M22 12a10 10 0 0 1-18.8 4.2"/>
          </svg>
        </button>
        
        <!-- 全屏按钮 -->
        <button class="fullscreen-btn" @click="toggleFullscreen">
          <svg v-if="!isFullscreen" t="1778255017172" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5587" width="18" height="18">
            <path d="M145.066667 85.333333h153.6c25.6 0 42.666667-17.066667 42.666666-42.666666S324.266667 0 298.666667 0H34.133333C25.6 0 17.066667 8.533333 8.533333 17.066667 0 25.6 0 34.133333 0 42.666667v256c0 25.6 17.066667 42.666667 42.666667 42.666666s42.666667-17.066667 42.666666-42.666666V145.066667l230.4 230.4c17.066667 17.066667 42.666667 17.066667 59.733334 0 17.066667-17.066667 17.066667-42.666667 0-59.733334L145.066667 85.333333z m170.666666 563.2L162.133333 802.133333l-76.8 76.8V725.333333C85.333333 699.733333 68.266667 682.666667 42.666667 682.666667s-42.666667 17.066667-42.666667 42.666666v256c0 25.6 17.066667 42.666667 42.666667 42.666667h256c25.6 0 42.666667-17.066667 42.666666-42.666667s-17.066667-42.666667-42.666666-42.666666H145.066667l76.8-76.8 153.6-153.6c17.066667-17.066667 17.066667-42.666667 0-59.733334-17.066667-17.066667-42.666667-17.066667-59.733334 0z m665.6 34.133334c-25.6 0-42.666667 17.066667-42.666666 42.666666v153.6l-76.8-76.8-153.6-153.6c-17.066667-17.066667-42.666667-17.066667-59.733334 0-17.066667 17.066667-17.066667 42.666667 0 59.733334l153.6 153.6 76.8 76.8H725.333333c-25.6 0-42.666667 17.066667-42.666666 42.666666s17.066667 42.666667 42.666666 42.666667h256c25.6 0 42.666667-17.066667 42.666667-42.666667v-256c0-25.6-17.066667-42.666667-42.666667-42.666666z m0-682.666667h-256c-25.6 0-42.666667 17.066667-42.666666 42.666667s17.066667 42.666667 42.666666 42.666666h153.6l-76.8 76.8-153.6 153.6c-17.066667 17.066667-17.066667 42.666667 0 59.733334 17.066667 17.066667 42.666667 17.066667 59.733334 0l153.6-153.6 76.8-76.8v153.6c0 25.6 17.066667 42.666667 42.666666 42.666666s42.666667-17.066667 42.666667-42.666666v-256c0-25.6-17.066667-42.666667-42.666667-42.666667z" fill="currentColor" p-id="5588"></path>
          </svg>
          <svg v-else t="1778255017172" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5587" width="18" height="18">
            <path d="M145.066667 85.333333h153.6c25.6 0 42.666667-17.066667 42.666666-42.666666S324.266667 0 298.666667 0H34.133333C25.6 0 17.066667 8.533333 8.533333 17.066667 0 25.6 0 34.133333 0 42.666667v256c0 25.6 17.066667 42.666667 42.666667 42.666666s42.666667-17.066667 42.666666-42.666666V145.066667l230.4 230.4c17.066667 17.066667 42.666667 17.066667 59.733334 0 17.066667-17.066667 17.066667-42.666667 0-59.733334L145.066667 85.333333z m170.666666 563.2L162.133333 802.133333l-76.8 76.8V725.333333C85.333333 699.733333 68.266667 682.666667 42.666667 682.666667s-42.666667 17.066667-42.666667 42.666666v256c0 25.6 17.066667 42.666667 42.666667 42.666667h256c25.6 0 42.666667-17.066667 42.666666-42.666667s-17.066667-42.666667-42.666666-42.666666H145.066667l76.8-76.8 153.6-153.6c17.066667-17.066667 17.066667-42.666667 0-59.733334-17.066667-17.066667-42.666667-17.066667-59.733334 0z m665.6 34.133334c-25.6 0-42.666667 17.066667-42.666666 42.666666v153.6l-76.8-76.8-153.6-153.6c-17.066667-17.066667-42.666667-17.066667-59.733334 0-17.066667 17.066667-17.066667 42.666667 0 59.733334l153.6 153.6 76.8 76.8H725.333333c-25.6 0-42.666667 17.066667-42.666666 42.666666s17.066667 42.666667 42.666666 42.666667h256c25.6 0 42.666667-17.066667 42.666667-42.666667v-256c0-25.6-17.066667-42.666667-42.666667-42.666666z m0-682.666667h-256c-25.6 0-42.666667 17.066667-42.666666 42.666667s17.066667 42.666667 42.666666 42.666666h153.6l-76.8 76.8-153.6 153.6c-17.066667 17.066667-17.066667 42.666667 0 59.733334 17.066667 17.066667 42.666667 17.066667 59.733334 0l153.6-153.6 76.8-76.8v153.6c0 25.6 17.066667 42.666667 42.666666 42.666666s42.666667-17.066667 42.666667-42.666666v-256c0-25.6-17.066667-42.666667-42.666667-42.666667z" fill="currentColor" p-id="5588"></path>
          </svg>
        </button>
        
        <!-- 分隔线 -->
        <div class="divider"></div>
        
        <!-- 时间显示 -->
        <div class="current-time">
          <span class="status-dot" :class="{ blink: isRefreshing }"></span>
          <span>{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <main class="dashboard-content">
      <!-- KPI指标卡片 -->
      <section class="kpi-section">
        <KPICard 
          type="active-users" 
          :value="kpiData.activeUsers" 
          :change="kpiData.activeUsersGrowth"
          label="活跃用户"
        />
        <KPICard 
          type="conversations" 
          :value="kpiData.totalConversations" 
          :change="kpiData.conversationsGrowth"
          label="会话数"
        />
        <KPICard 
          type="messages" 
          :value="kpiData.totalMessages" 
          :change="kpiData.messagesGrowth"
          label="消息数"
        />
        <KPICard 
          type="response-time" 
          :value="kpiData.avgResponseTime" 
          :change="kpiData.responseTimeChange"
          label="响应时间"
          unit="ms"
        />
        <KPICard 
          type="kb-hit-rate" 
          :value="kpiData.kbHitRate" 
          change="0"
          label="知识库命中率"
          unit="%"
        />
        <KPICard 
          type="success-rate" 
          :value="kpiData.successRate" 
          change="0"
          label="调用成功率"
          unit="%"
        />
      </section>

      <!-- 图表区域 -->
      <section class="charts-section">
        <div class="chart-row">
          <div class="chart-wrapper large">
            <TrendChart 
              title="会话趋势" 
              :data="trendData"
              chart-type="mix"
            />
          </div>
          <div class="chart-wrapper">
            <DistributionChart 
              title="消息来源分布" 
              :data="distributionData"
              chart-type="donut"
            />
          </div>
        </div>
        <div class="chart-row">
          <div class="chart-wrapper">
            <HotQuestions 
              title="热点问题" 
              :questions="hotQuestions"
              @refresh="fetchHotQuestions"
            />
          </div>
          <div class="chart-wrapper large">
            <RealtimeList 
              title="实时会话" 
              :items="realtimeSessions"
            />
          </div>
        </div>
      </section>
    </main>

    <!-- 消息提示 -->
    <div v-if="showToast" class="toast" :class="toastType">
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import KPICard from './components/KPICard.vue'
import TrendChart from './components/TrendChart.vue'
import DistributionChart from './components/DistributionChart.vue'
import RealtimeList from './components/RealtimeList.vue'
import HotQuestions from './components/HotQuestions.vue'

// 时间范围选项
const timeRanges = [
  { label: '24小时', value: '24h' },
  { label: '7天', value: '7d' },
  { label: '30天', value: '30d' }
]

// 响应式状态
const selectedRange = ref('24h')
const isRefreshing = ref(false)
const isFullscreen = ref(false)
const currentTime = ref('')
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// 数据状态
const kpiData = reactive({
  activeUsers: 0,
  totalConversations: 0,
  totalMessages: 0,
  avgResponseTime: 0,
  kbHitRate: 0,
  successRate: 0,
  activeUsersGrowth: 0,
  conversationsGrowth: 0,
  messagesGrowth: 0,
  responseTimeChange: 0
})

const trendData = reactive({
  labels: [],
  sessionCounts: [],
  messageCounts: [],
  responseTimes: []
})

const distributionData = ref([])
const hotQuestions = ref([])
const realtimeSessions = ref([])

// SSE连接
let eventSource = null

// 更新当前时间
const updateCurrentTime = () => {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${month}/${day} ${hours}:${minutes}:${seconds}`
}

// 显示提示消息
const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

// 选择时间范围
const selectTimeRange = (range) => {
  if (selectedRange.value === range) return
  selectedRange.value = range
  refreshData()
}

// 刷新数据
const refreshData = async () => {
  isRefreshing.value = true
  updateCurrentTime()
  
  try {
    await Promise.all([
      fetchKPI(),
      fetchTrend(),
      fetchDistribution()
    ])
    showToastMessage('数据刷新成功', 'success')
  } catch (error) {
    console.error('刷新数据失败:', error)
    showToastMessage('刷新数据失败', 'error')
  } finally {
    isRefreshing.value = false
  }
}

// 解析数值（处理BigDecimal序列化对象和各种格式）
const parseNumber = (val) => {
  if (val === null || val === undefined) return 0
  // 处理BigDecimal序列化对象 {value: "xxx", scale: x}
  if (typeof val === 'object' && val.value !== undefined) {
    const num = Number(val.value)
    return isNaN(num) ? 0 : num
  }
  const num = Number(val)
  return isNaN(num) ? 0 : num
}

// 获取KPI数据
const fetchKPI = async () => {
  try {
    const response = await axios.get(`/agent/statistics/dashboard/kpi`, {
      params: { timeRange: selectedRange.value }
    })
    console.log('KPI响应数据:', response.data)
    if (response.data.code === '0') {
      const data = response.data.data
      console.log('KPI数据:', data)
      console.log('activeUsers类型:', typeof data.activeUsers, '值:', data.activeUsers)
      // 将BigDecimal类型转换为数字
      kpiData.activeUsers = parseNumber(data.activeUsers)
      kpiData.totalConversations = parseNumber(data.totalConversations)
      kpiData.totalMessages = parseNumber(data.totalMessages)
      kpiData.avgResponseTime = parseNumber(data.avgResponseTime)
      kpiData.kbHitRate = parseNumber(data.kbHitRate)
      kpiData.successRate = parseNumber(data.successRate)
      kpiData.activeUsersGrowth = parseNumber(data.activeUsersGrowth)
      kpiData.conversationsGrowth = parseNumber(data.conversationsGrowth)
      kpiData.messagesGrowth = parseNumber(data.messagesGrowth)
      kpiData.responseTimeChange = parseNumber(data.responseTimeChange)
      console.log('更新后的kpiData:', kpiData)
    }
  } catch (error) {
    console.error('获取KPI数据失败:', error)
  }
}

// 获取趋势数据
const fetchTrend = async () => {
  try {
    const response = await axios.get(`/agent/statistics/dashboard/trend`, {
      params: { 
        timeRange: selectedRange.value,
        type: 'session'
      }
    })
    if (response.data.code === '0') {
      const data = response.data.data
      Object.assign(trendData, data)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 获取分布数据
const fetchDistribution = async () => {
  try {
    const response = await axios.get(`/agent/statistics/dashboard/message-distribution`, {
      params: { timeRange: selectedRange.value }
    })
    if (response.data.code === '0') {
      distributionData.value = response.data.data
    }
  } catch (error) {
    console.error('获取分布数据失败:', error)
  }
}

// 获取热点问题
const fetchHotQuestions = async () => {
  try {
    const response = await axios.get('/agent/statistics/dashboard/hot-questions', {
      params: { limit: 10 }
    })
    if (response.data.code === '0') {
      hotQuestions.value = response.data.data
    }
  } catch (error) {
    console.error('获取热点问题失败:', error)
  }
}

// 获取实时会话
const fetchRealtimeSessions = async () => {
  try {
    const response = await axios.get('/agent/statistics/dashboard/realtime-sessions', {
      params: { limit: 20 }
    })
    if (response.data.code === '0') {
      realtimeSessions.value = response.data.data
    }
  } catch (error) {
    console.error('获取实时会话失败:', error)
  }
}

// 处理趋势图标签切换
const handleTrendTabChange = (tab) => {
  selectedRange.value = tab
  fetchTrend()
}

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 初始化SSE连接
const initSSE = () => {
  try {
    eventSource = new EventSource('/agent/statistics/dashboard/realtime')
    
    eventSource.addEventListener('realtime-event', (event) => {
      try {
        const data = JSON.parse(event.data)
        // 将新事件添加到列表头部
        realtimeSessions.value.unshift(data)
        // 保持列表长度限制
        if (realtimeSessions.value.length > 20) {
          realtimeSessions.value.pop()
        }
      } catch (error) {
        console.error('解析实时事件失败:', error)
      }
    })

    eventSource.addEventListener('heartbeat', () => {
      // 心跳保持连接
    })

    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      eventSource.close()
      // 3秒后重新连接
      setTimeout(initSSE, 3000)
    }
  } catch (error) {
    console.error('初始化SSE连接失败:', error)
  }
}

// 时间更新定时器
let timeTimer = null

onMounted(() => {
  // 初始化时间
  updateCurrentTime()
  timeTimer = setInterval(updateCurrentTime, 1000)
  
  // 获取初始数据
  refreshData()
  fetchHotQuestions()
  fetchRealtimeSessions()
  
  // 初始化SSE连接
  initSSE()

  // 监听全屏变化
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 700;
  color: #1D2129;
  margin: 0;
}

.time-select-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.divider {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
}

.time-range-selector {
  display: flex;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 4px;
  gap: 4px;
}

.range-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  border-radius: 6px;
  transition: all 0.25s ease;
}

.range-btn:hover {
  color: #667eea;
}

.range-btn.active {
  background: #ffffff;
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.refresh-btn,
.fullscreen-btn {
  padding: 8px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn:hover,
.fullscreen-btn:hover {
  background: #e5e7eb;
  color: #667eea;
}

.refresh-btn svg,
.fullscreen-btn svg {
  display: block;
}

.refresh-btn.refreshing svg {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.current-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  padding: 8px 14px;
  background: #f9fafb;
  border-radius: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
}

.status-dot.blink {
  animation: blink 1s ease-out;
}

@keyframes blink {
  0% { box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(82, 196, 26, 0); }
  100% { box-shadow: 0 0 0 0 rgba(82, 196, 26, 0); }
}

.dashboard-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
}

.kpi-section {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.charts-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-row {
  display: flex;
  gap: 24px;
}

.chart-wrapper {
  flex: 1;
  min-height: 350px;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chart-wrapper.large {
  flex: 2;
}

.toast {
  position: fixed;
  top: 100px;
  right: 24px;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast.success {
  background: rgba(0, 180, 42, 0.9);
  color: white;
}

.toast.error {
  background: rgba(245, 63, 63, 0.9);
  color: white;
}

.toast.info {
  background: rgba(102, 126, 234, 0.9);
  color: white;
}

@media (max-width: 1200px) {
  .chart-row {
    flex-direction: column;
  }
  
  .chart-wrapper.large {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }
  
  .logo-title {
    font-size: 16px;
  }
  
  .kpi-section {
    justify-content: center;
  }
}

/* 全局自定义滚动条样式 */
.dashboard-content::-webkit-scrollbar {
  width: 8px;
}

.dashboard-content::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.dashboard-content::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.dashboard-content::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
