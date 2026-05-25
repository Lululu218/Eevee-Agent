
<template>
  <div class="distribution-chart">
    <div class="chart-header">
      <h3 class="chart-title">{{ title }}</h3>
    </div>
    <div ref="chartRef" class="chart-container"></div>
    <div class="chart-legend">
      <div 
        v-for="(item, index) in data" 
        :key="index" 
        class="legend-item"
      >
        <span class="legend-color" :style="{ background: item.color }"></span>
        <span class="legend-name">{{ item.name }}</span>
        <span class="legend-value">{{ item.value.toLocaleString() }}</span>
        <span class="legend-percent">{{ item.percentage }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: {
    type: String,
    default: '分布'
  },
  data: {
    type: Array,
    default: () => []
  },
  chartType: {
    type: String,
    default: 'pie' // pie, donut, bar
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
  if (!chartInstance || !props.data || props.data.length === 0) return

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#374151' },
      padding: [12, 16],
      extraCssText: 'box-shadow: 0 4px 20px rgba(0,0,0,0.1); border-radius: 8px;',
      formatter: (params) => {
        return `<div style="padding: 4px;">
          <div style="font-weight: 600; margin-bottom: 8px; font-size: 14px;">${params.name}</div>
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
            <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${params.color};"></span>
            <span style="color: #6b7280;">数量:</span>
            <span style="font-weight: 600; color: #1D2129;">${params.value.toLocaleString()}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="color: #6b7280;">占比:</span>
            <span style="font-weight: 600; color: #667eea;">${params.percent}%</span>
          </div>
        </div>`
      }
    },
    animationDuration: 1500,
    animationEasing: 'cubicInOut'
  }

  if (props.chartType === 'donut') {
    option.series = [{
      name: props.title,
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        scale: true,
        scaleSize: 10,
        itemStyle: {
          shadowBlur: 15,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      labelLine: { show: false },
      data: props.data.map((item, index) => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: item.color },
        animationDelay: index * 100
      }))
    }]
  } else if (props.chartType === 'bar') {
    option.grid = {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    }
    option.xAxis = {
      type: 'category',
      data: props.data.map(item => item.name),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 12 },
      axisTick: { show: false }
    }
    option.yAxis = {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6b7280', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } }
    }
    option.series = [{
      type: 'bar',
      data: props.data.map((item, index) => ({
        value: item.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: item.color },
            { offset: 1, color: adjustColor(item.color, -20) }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        animationDelay: index * 100
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.1)'
        }
      }
    }]
  } else {
    option.series = [{
      name: props.title,
      type: 'pie',
      radius: ['0%', '70%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        scale: true,
        scaleSize: 10,
        itemStyle: {
          shadowBlur: 15,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      labelLine: { show: false },
      data: props.data.map((item, index) => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: item.color },
        animationDelay: index * 100
      }))
    }]
  }

  chartInstance.setOption(option, true)
}

const adjustColor = (color, amount) => {
  const hex = color.replace('#', '')
  const num = parseInt(hex, 16)
  const r = Math.min(255, Math.max(0, (num >> 16) + amount))
  const g = Math.min(255, Math.max(0, ((num >> 8) & 0x00FF) + amount))
  const b = Math.min(255, Math.max(0, (num & 0x0000FF) + amount))
  return `#${(r << 16 | g << 8 | b).toString(16).padStart(6, '0')}`
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
.distribution-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
}

.chart-container {
  flex: 1;
  min-height: 200px;
}

.chart-legend {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.legend-color {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-name {
  font-size: 13px;
  color: #4b5563;
  font-weight: 500;
}

.legend-value {
  font-size: 13px;
  color: #1D2129;
  font-weight: 600;
}

.legend-percent {
  font-size: 12px;
  color: #86909C;
  padding-left: 4px;
}
</style>
