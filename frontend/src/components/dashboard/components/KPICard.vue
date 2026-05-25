<template>
  <div class="kpi-card" :class="[`kpi-card--${type}`]" :style="cardStyle">
    <div class="card-body">
      <div class="card-top">
        <div class="card-icon" :style="iconStyle">
          <!-- 活跃用户专用图标 -->
          <svg v-if="type === 'active-users'" t="1778259852808" class="icon" viewBox="0 0 1050 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="19644" width="24" height="24">
            <path d="M692.224 429.659897h-53.720615c0 64.590769-56.083692 116.946051-125.29559 116.946052s-125.321846-52.355282-125.321846-116.946052h-53.720616c0 92.265026 80.160821 167.069538 179.042462 167.069539 98.855385 0 179.016205-74.804513 179.016205-167.069539" fill="currentColor" p-id="19645"></path>
            <path d="M1049.101128 598.226051c0-90.847179-51.331282-169.878974-126.949743-210.628923C918.659282 173.108513 742.137436 0.262564 524.550564 0.262564 306.937436 0.236308 130.389333 173.134769 126.949744 387.675897 51.305026 428.452103 0 507.431385 0 598.226051c0 110.880821 76.563692 203.933538 180.486564 231.50277l-54.350769 172.767179h54.744615l18.090667-53.668103 48.941949-161.476923c-1.260308 0.052513-2.441846 0.393846-3.728411 0.393847-104.894359 0-189.912615-84.151795-189.912615-187.943385 0-61.650051 30.404923-115.895795 76.773744-150.16041 20.558769 143.438769 119.072821 261.723897 251.798974 311.873641l-58.551795 187.31323-16.804102 53.694359h54.272l18.090666-53.694359 77.797744-221.341538C299.848205 696.68759 180.88041 559.130256 180.88041 393.977436c0-187.812103 153.862564-340.046769 343.670154-340.046769s343.670154 152.234667 343.670154 340.046769c0 165.152821-118.994051 302.710154-276.742564 333.508923l77.771487 221.341538 18.090667 53.694359h54.245743l-16.935384-53.694359h0.157538l-58.551795-187.339487c132.726154-50.149744 231.266462-168.461128 251.798975-311.926154 46.368821 34.264615 76.826256 88.536615 76.826256 150.23918 0 103.79159-84.965744 187.943385-189.781333 187.943385-1.286564 0-2.468103-0.341333-3.754667-0.36759l48.915692 161.450666h-0.131282l18.064411 53.694359h54.823384l-54.298256-172.767179c103.844103-27.569231 180.381538-120.648205 180.381538-231.529026" fill="currentColor" p-id="19646"></path>
          </svg>
          <!-- 会话卡片专用图标 -->
          <svg v-else-if="type === 'conversations'" t="1778255104771" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12539" width="24" height="24">
            <path d="M525.942857 1008.8c-73.714286 0-146.742857-18.628571-210.971428-53.942857-11.085714-6.057143-15.085714-20-9.028572-31.085714 6.057143-11.085714 20-15.085714 31.085714-9.028572 57.485714 31.657143 122.857143 48.342857 188.914286 48.342857 62.171429 0 121.6-14.057143 176.685714-41.828571 11.314286-5.714286 25.028571-1.142857 30.742858 10.171428s1.142857 25.028571-10.171429 30.742858c-61.485714 30.857143-127.885714 46.628571-197.257143 46.628571zM940.685714 622.857143c-0.571429 0-1.142857 0-1.6-0.114286-12.571429-0.914286-22.057143-11.771429-21.142857-24.457143 0.685714-9.371429 1.028571-18.857143 1.028572-28.228571 0-70.628571-18.971429-139.885714-54.742858-200.228572-34.857143-58.628571-84.571429-107.542857-143.771428-141.371428-10.971429-6.285714-14.742857-20.228571-8.571429-31.2 6.285714-10.971429 20.228571-14.742857 31.2-8.571429 66.057143 37.714286 121.6 92.228571 160.457143 157.714286 40 67.428571 61.142857 144.8 61.142857 223.657143 0 10.514286-0.342857 21.142857-1.142857 31.542857-0.914286 12.114286-10.971429 21.257143-22.857143 21.257143zM111.085714 622.857143c-11.885714 0-21.942857-9.142857-22.742857-21.257143-0.685714-10.4-1.142857-21.028571-1.142857-31.542857 0-145.6 72-281.371429 192.571429-363.2 10.4-7.085714 24.685714-4.342857 31.771428 6.057143s4.342857 24.685714-6.057143 31.771428c-108 73.371429-172.571429 194.971429-172.571428 325.371429 0 9.371429 0.342857 18.971429 1.028571 28.228571 0.914286 12.571429-8.571429 23.542857-21.142857 24.457143-0.571429 0.114286-1.142857 0.114286-1.714286 0.114286zM157.6 953.371429C70.971429 953.371429 0.457143 882.857143 0.457143 796.228571s70.514286-157.142857 157.142857-157.142857 157.142857 70.514286 157.142857 157.142857-70.514286 157.142857-157.142857 157.142858z m0-268.571429c-61.485714 0-111.428571 49.942857-111.428571 111.428571s49.942857 111.428571 111.428571 111.428572 111.428571-49.942857 111.428571-111.428572-49.942857-111.428571-111.428571-111.428571zM866.857143 953.371429c-86.628571 0-157.142857-70.514286-157.142857-157.142858s70.514286-157.142857 157.142857-157.142857S1024 709.6 1024 796.228571s-70.4 157.142857-157.142857 157.142858z m0-268.571429c-61.485714 0-111.428571 49.942857-111.428572 111.428571s49.942857 111.428571 111.428572 111.428572 111.428571-49.942857 111.428571-111.428572-49.942857-111.428571-111.428571-111.428571zM511.885714 329.6c-86.628571 0-157.142857-70.514286-157.142857-157.142857S425.257143 15.314286 511.885714 15.314286c34.742857 0 67.771429 11.085714 95.314286 32.228571 10.057143 7.657143 12 22.057143 4.342857 32-7.657143 10.057143-22.057143 12-32 4.342857-19.542857-14.857143-42.857143-22.742857-67.542857-22.742857-61.485714 0-111.428571 49.942857-111.428571 111.428572s49.942857 111.428571 111.428571 111.428571 111.428571-49.942857 111.428571-111.428571c0-15.085714-2.971429-29.714286-8.8-43.542858-4.914286-11.657143 0.457143-25.028571 12.114286-29.942857 11.657143-4.914286 25.028571 0.457143 29.942857 12.114286 8.228571 19.428571 12.457143 40.114286 12.457143 61.371429-0.114286 86.514286-70.628571 157.028571-157.257143 157.028571z" fill="currentColor" p-id="12540"></path>
          </svg>
          <!-- 其他卡片图标 -->
          <svg v-else-if="iconPath" :width="24" :height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path :d="iconPath"/>
          </svg>
          <svg v-else-if="iconPaths" :width="24" :height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path v-for="(path, idx) in iconPaths" :key="idx" :d="path"/>
          </svg>
        </div>
        <div class="card-change" :class="changeClass">
          <span class="change-icon">{{ changeIcon }}</span>
          <span class="change-value">{{ changeValue }}</span>
        </div>
      </div>
      <div class="card-bottom">
        <div class="card-value-wrapper">
          <span class="card-value">{{ formattedValue }}</span>
          <span v-if="unit" class="card-unit">{{ unit }}</span>
        </div>
        <div class="card-label">{{ displayLabel }}</div>
      </div>
    </div>
    <div class="card-decoration"></div>
    <div class="card-glow"></div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => [
      'active-users', 
      'conversations', 
      'messages', 
      'response-time',
      'kb-hit-rate',
      'success-rate'
    ].includes(value)
  },
  value: {
    type: [Number, String],
    required: true
  },
  change: {
    type: [Number, String],
    default: 0
  },
  label: {
    type: String,
    default: ''
  },
  unit: {
    type: String,
    default: ''
  },
  animated: {
    type: Boolean,
    default: true
  }
})

// 专业SVG图标路径
const iconConfig = {
  'active-users': { 
    paths: ['M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2'],
    gradient: ['#6366f1', '#8b5cf6']
  },
  'conversations': { 
    paths: ['M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'],
    gradient: ['#8b5cf6', '#a78bfa']
  },
  'messages': { 
    paths: ['M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z'],
    gradient: ['#f59e0b', '#fbbf24']
  },
  'response-time': { 
    paths: ['M12 8v4l3 3'],
    gradient: ['#06b6d4', '#22d3ee']
  },
  'kb-hit-rate': { 
    paths: ['M12 20h9', 'M16.5 3.5a2.121 2.121 0 0 1 3 3L7.5 18H4v-3.5L16.5 3.5z'],
    gradient: ['#10b981', '#34d399']
  },
  'success-rate': { 
    paths: ['M22 11.08V12a10 10 0 1 1-5.93-9.14'],
    gradient: ['#16a34a', '#22c55e']
  }
}

const iconPath = computed(() => iconConfig[props.type]?.path)
const iconPaths = computed(() => iconConfig[props.type]?.paths)
const iconGradient = computed(() => iconConfig[props.type]?.gradient || ['#667eea', '#36BFFA'])

const typeConfig = {
  'active-users': { label: '活跃用户', gradient: ['#6366f1', '#8b5cf6'] },
  'conversations': { label: '会话数', gradient: ['#8b5cf6', '#a78bfa'] },
  'messages': { label: '消息数', gradient: ['#f59e0b', '#fbbf24'] },
  'response-time': { label: '响应时间', gradient: ['#06b6d4', '#22d3ee'] },
  'kb-hit-rate': { label: '知识库命中率', gradient: ['#10b981', '#34d399'] },
  'success-rate': { label: '调用成功率', gradient: ['#16a34a', '#22c55e'] }
}

const displayLabel = computed(() => props.label || typeConfig[props.type].label)

const cardStyle = computed(() => {
  const gradient = typeConfig[props.type].gradient
  return {
    '--card-gradient-start': gradient[0],
    '--card-gradient-end': gradient[1]
  }
})

const iconStyle = computed(() => ({
  background: `linear-gradient(135deg, ${iconGradient.value[0]}, ${iconGradient.value[1]})`
}))

const displayValue = ref(0)
const isAnimating = ref(false)

const formatNumber = (num) => {
  const parsedNum = parseFloat(num)
  if (isNaN(parsedNum)) return '0'
  
  if (props.type === 'kb-hit-rate' || props.type === 'success-rate') {
    return parsedNum.toFixed(1)
  }
  
  if (props.type === 'response-time') {
    return Math.round(parsedNum)
  }
  
  if (parsedNum >= 1000000) {
    return (parsedNum / 1000000).toFixed(2) + 'M'
  } else if (parsedNum >= 1000) {
    return (parsedNum / 1000).toFixed(1) + 'K'
  }
  return Math.round(parsedNum).toLocaleString()
}

const formattedValue = computed(() => formatNumber(displayValue.value))

const animateValue = (start, end, duration = 1800) => {
  isAnimating.value = true
  const startTime = performance.now()
  const startNum = parseFloat(start) || 0
  const endNum = parseFloat(end) || 0
  
  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOutExpo = 1 - Math.pow(2, -10 * progress)
    displayValue.value = startNum + (endNum - startNum) * easeOutExpo
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      displayValue.value = endNum
      isAnimating.value = false
    }
  }
  requestAnimationFrame(animate)
}

watch(() => props.value, (newVal) => {
  const numValue = Number(newVal)
  if (props.animated && !isAnimating.value) {
    animateValue(displayValue.value || 0, numValue)
  } else {
    displayValue.value = numValue
  }
})

onMounted(() => {
  const numValue = Number(props.value)
  if (props.animated) {
    animateValue(0, numValue)
  } else {
    displayValue.value = numValue
  }
})

const changeClass = computed(() => {
  const changeNum = parseFloat(props.change) || 0
  if (changeNum > 0) return 'positive'
  if (changeNum < 0) return 'negative'
  return 'neutral'
})

const changeIcon = computed(() => {
  const changeNum = parseFloat(props.change) || 0
  if (changeNum > 0) return '↑'
  if (changeNum < 0) return '↓'
  return '→'
})

const changeValue = computed(() => {
  const changeNum = parseFloat(props.change) || 0
  return Math.abs(changeNum).toFixed(1) + '%'
})
</script>

<style scoped>
.kpi-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(22, 93, 255, 0.08);
  border-radius: 20px;
  padding: 28px;
  width: 180px;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.04),
    0 1px 3px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.kpi-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.08),
    0 4px 12px rgba(0, 0, 0, 0.04),
    0 0 0 1px rgba(102, 126, 234, 0.15);
}

.card-decoration {
  position: absolute;
  top: -20%;
  right: -20%;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, var(--card-gradient-start, #667eea), var(--card-gradient-end, #36BFFA));
  opacity: 0.05;
  border-radius: 50%;
  transition: all 0.4s ease;
}

.kpi-card:hover .card-decoration {
  opacity: 0.08;
  transform: scale(1.2);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, transparent 50%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.kpi-card:hover .card-glow {
  opacity: 1;
}

.card-body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.kpi-card:hover .card-icon {
  transform: scale(1.05);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
}

.card-icon::before {
  content: '';
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
}

.card-icon svg {
  position: relative;
  z-index: 1;
}

.card-change {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 600;
  gap: 3px;
  padding: 5px 10px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.card-change.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.card-change.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.card-change.neutral {
  color: #6b7280;
  background: rgba(107, 114, 128, 0.1);
}

.change-icon {
  font-size: 10px;
  line-height: 1;
}

.card-bottom {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.card-value {
  font-size: 36px;
  font-weight: 700;
  color: #111827;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  letter-spacing: -0.02em;
  line-height: 1.1;
  transition: all 0.3s ease;
}

.kpi-card:hover .card-value {
  color: var(--card-gradient-start, #667eea);
}

.card-unit {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
}

.card-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  letter-spacing: 0.02em;
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.kpi-card.loading {
  position: relative;
}

.kpi-card.loading::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  100% {
    left: 100%;
  }
}

@media (max-width: 768px) {
  .kpi-card {
    width: 100%;
    max-width: 280px;
  }
  
  .card-value {
    font-size: 32px;
  }
}
</style>
