
<template>
  <div class="trend-chart">
    <div class="chart-header">
      <h3 class="chart-title">{{ title }}</h3>
    </div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: {
    type: String,
    default: '趋势图'
  },
  data: {
    type: Object,
    default: () => ({
      labels: [],
      sessionCounts: [],
      messageCounts: [],
      responseTimes: []
    })
  },
  chartType: {
    type: String,
    default: 'mix' // line, bar, mix
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance || !props.data) return

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#374151' },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#999' },
        lineStyle: {
          color: '#667eea',
          width: 1,
          type: 'dashed'
        }
      },
      padding: [12, 16],
      extraCssText: 'box-shadow: 0 4px 20px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    legend: {
      data: ['会话数', '消息数'],
      bottom: 0,
      textStyle: { color: '#6b7280', fontSize: 12 },
      itemWidth: 16,
      itemHeight: 8,
      itemGap: 16
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: props.chartType === 'bar' || props.chartType === 'mix',
      data: props.data.labels || [],
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { 
        color: '#6b7280',
        fontSize: 12
      },
      axisTick: { show: false },
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(102, 126, 234, 0.05)'
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '数量',
        nameTextStyle: { color: '#6b7280', fontSize: 12, padding: [0, 0, 0, 40] },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { 
          color: '#6b7280',
          fontSize: 12
        },
        splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } }
      }
    ],
    series: [],
    animationDuration: 1500,
    animationEasing: 'cubicInOut'
  }

  if (props.chartType === 'mix') {
    option.series = [
      {
        name: '会话数',
        type: 'line',
        smooth: true,
        data: props.data.sessionCounts || [],
        lineStyle: { color: '#667eea', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.35)' },
            { offset: 0.5, color: 'rgba(102, 126, 234, 0.15)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { 
          color: '#667eea',
          borderWidth: 2,
          borderColor: '#fff'
        },
        emphasis: {
          scale: true,
          itemStyle: {
            color: '#667eea',
            borderWidth: 3,
            shadowBlur: 10,
            shadowColor: 'rgba(102, 126, 234, 0.5)'
          }
        },
        animationDelay: (idx) => idx * 100
      },
      {
        name: '消息数',
        type: 'bar',
        data: props.data.messageCounts || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#36BFFA' },
            { offset: 1, color: '#165DFF' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#5BC4FF' },
              { offset: 1, color: '#36BFFA' }
            ]),
            shadowBlur: 10,
            shadowColor: 'rgba(54, 191, 250, 0.5)'
          }
        },
        animationDelay: (idx) => idx * 50 + 300
      }
    ]
  } else if (props.chartType === 'line') {
    option.series = [
      {
        name: '会话数',
        type: 'line',
        smooth: true,
        data: props.data.sessionCounts || [],
        lineStyle: { color: '#667eea', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.35)' },
            { offset: 0.5, color: 'rgba(102, 126, 234, 0.15)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { 
          color: '#667eea',
          borderWidth: 2,
          borderColor: '#fff'
        },
        emphasis: {
          scale: true,
          itemStyle: {
            color: '#667eea',
            borderWidth: 3,
            shadowBlur: 10,
            shadowColor: 'rgba(102, 126, 234, 0.5)'
          }
        }
      }
    ]
  } else {
    option.series = [
      {
        name: '会话数',
        type: 'bar',
        data: props.data.sessionCounts || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#36BFFA' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#8B9FFF' },
              { offset: 1, color: '#667eea' }
            ]),
            shadowBlur: 10,
            shadowColor: 'rgba(102, 126, 234, 0.5)'
          }
        }
      }
    ]
  }

  chartInstance.setOption(option, true)
}

watch(() => props.data, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.trend-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
}

.chart-tabs {
  display: flex;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 4px;
  gap: 4px;
}

.tab-btn {
  padding: 6px 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  border-radius: 6px;
  transition: all 0.25s ease;
}

.tab-btn:hover {
  color: #667eea;
}

.tab-btn.active {
  background: #ffffff;
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.chart-container {
  flex: 1;
  min-height: 300px;
}
</style>
