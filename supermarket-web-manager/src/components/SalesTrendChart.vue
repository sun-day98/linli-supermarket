<template>
  <div ref="chartEl" class="sales-trend" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { StatsTrendVO } from '@/types'

const props = defineProps<{ data: StatsTrendVO }>()
const chartEl = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

function render(): void {
  if (!chart) return
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'], right: 10 },
    grid: { left: 50, right: 50, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: props.data.dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '销售额(元)' },
      { type: 'value', name: '订单数', minInterval: 1 },
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: props.data.sales,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#07c160' },
      },
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: props.data.orders,
        itemStyle: { color: '#409eff' },
      },
    ],
  })
}

function onResize(): void {
  chart?.resize()
}

onMounted(() => {
  if (chartEl.value) {
    chart = echarts.init(chartEl.value)
    render()
  }
  window.addEventListener('resize', onResize)
})

watch(() => props.data, render, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.sales-trend {
  width: 100%;
  height: 320px;
}
</style>
