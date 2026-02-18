<template>
  <el-row :gutter="20">
    <el-col :span="8">
      <el-card>
        <template #header>Total Events</template>
        <div class="stat-value">{{ totalCount }}</div>
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card>
        <template #header>Success Rate</template>
        <div class="stat-value">{{ successRate }}%</div>
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card>
        <template #header>Failed Events</template>
        <div class="stat-value text-danger">{{ failedCount }}</div>
      </el-card>
    </el-col>
  </el-row>
  
  <el-row :gutter="20" style="margin-top: 20px">
    <el-col :span="12">
      <el-card>
        <template #header>State Distribution</template>
        <div ref="pieChart" style="height: 300px"></div>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header>Event Trend (24h)</template>
        <div ref="lineChart" style="height: 300px"></div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/event'

const stats = ref<any>({})
const pieChart = ref<HTMLElement>()
const lineChart = ref<HTMLElement>()

const totalCount = computed(() => {
  if (!stats.value.stateDistribution) return 0
  return stats.value.stateDistribution.reduce((acc: number, cur: any) => acc + cur.count, 0)
})

const failedCount = computed(() => {
  if (!stats.value.stateDistribution) return 0
  const failed = stats.value.stateDistribution.find((s: any) => s.state === 'PROCESS_FAILED')
  return failed ? failed.count : 0
})

const successRate = computed(() => {
  if (totalCount.value === 0) return 0
  const success = stats.value.stateDistribution?.find((s: any) => s.state === 'PROCESS_COMPLETE')
  return success ? ((success.count / totalCount.value) * 100).toFixed(2) : 0
})

const initCharts = () => {
  if (pieChart.value && stats.value.stateDistribution) {
    const chart = echarts.init(pieChart.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: stats.value.stateDistribution.map((s: any) => ({
            value: s.count,
            name: s.state
          }))
        }
      ]
    })
  }
  
  if (lineChart.value && stats.value.trend) {
    const chart = echarts.init(lineChart.value)
    chart.setOption({
      xAxis: {
        type: 'category',
        data: stats.value.trend.map((t: any) => t.time)
      },
      yAxis: { type: 'value' },
      series: [
        {
          data: stats.value.trend.map((t: any) => t.count),
          type: 'line'
        }
      ]
    })
  }
}

onMounted(async () => {
  try {
    stats.value = await getDashboardStats()
    initCharts()
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.stat-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
}
.text-danger {
  color: #f56c6c;
}
</style>
