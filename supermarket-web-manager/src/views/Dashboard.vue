<template>
  <div>
    <PageHeader title="数据概览" description="今日经营状况与近期销售趋势">
      <el-radio-group v-model="days" @change="loadTrend">
        <el-radio-button :value="7">近7日</el-radio-button>
        <el-radio-button :value="14">近14日</el-radio-button>
        <el-radio-button :value="30">近30日</el-radio-button>
      </el-radio-group>
      <el-button @click="loadAll">刷新</el-button>
    </PageHeader>

    <el-row :gutter="14">
      <el-col v-for="card in cards" :key="card.title" :xs="12" :sm="8" :md="6">
        <StatCard :title="card.title" :value="card.value" :icon="card.icon" :color="card.color" :tip="card.tip" />
      </el-col>
    </el-row>

    <el-card style="margin-top: 14px">
      <template #header><span>销售趋势</span></template>
      <SalesTrendChart :data="trend" />
    </el-card>

    <el-card style="margin-top: 14px">
      <template #header><span>最新订单</span></template>
      <el-table :data="latestOrders" size="small" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="receiver" label="收货人" width="100" />
        <el-table-column label="实付金额" width="110">
          <template #default="{ row }"><span class="money">¥{{ row.payAmount.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="ORDER_STATUS_TAG[row.status] || 'info'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import SalesTrendChart from '@/components/SalesTrendChart.vue'
import { getLatestOrders, getOverview, getTrend } from '@/api/stats'
import { ORDER_STATUS_TAG } from '@/types'
import type { OrderVO, StatsOverviewVO, StatsTrendVO } from '@/types'

const overview = ref<StatsOverviewVO>({
  todaySales: 0, todayOrders: 0, avgOrderAmount: 0, newMembers: 0,
  pendingPayOrders: 0, deliveringOrders: 0, lowStockProducts: 0,
  totalMembers: 0, totalOrders: 0, totalSales: 0,
})
const trend = ref<StatsTrendVO>({ dates: [], sales: [], orders: [] })
const latestOrders = ref<OrderVO[]>([])
const days = ref(7)

const cards = computed(() => [
  { title: '今日销售额', value: `¥${overview.value.todaySales.toFixed(2)}`, icon: 'Money', color: '#f2270c', tip: '已付款订单口径' },
  { title: '今日订单', value: overview.value.todayOrders, icon: 'ShoppingCart', color: '#07c160', tip: '今日全部下单量' },
  { title: '客单价', value: `¥${overview.value.avgOrderAmount.toFixed(2)}`, icon: 'TrendCharts', color: '#409eff', tip: '今日销售额 / 订单数' },
  { title: '新增会员', value: overview.value.newMembers, icon: 'UserFilled', color: '#9b59b6', tip: '今日注册会员' },
  { title: '待付款订单', value: overview.value.pendingPayOrders, icon: 'Wallet', color: '#ff976a', tip: '需关注未支付' },
  { title: '配送中订单', value: overview.value.deliveringOrders, icon: 'Truck', color: '#13c2c2', tip: '在途订单' },
  { title: '库存紧张商品', value: overview.value.lowStockProducts, icon: 'Warning', color: '#f5222d', tip: '库存低于阈值' },
  { title: '累计销售额', value: `¥${overview.value.totalSales.toFixed(2)}`, icon: 'DataLine', color: '#606266', tip: `会员 ${overview.value.totalMembers} · 订单 ${overview.value.totalOrders}` },
])

async function loadTrend(): Promise<void> {
  trend.value = await getTrend(days.value)
}

async function loadAll(): Promise<void> {
  const [stats, chart, orders] = await Promise.all([
    getOverview(),
    getTrend(days.value),
    getLatestOrders(6),
  ])
  overview.value = stats
  trend.value = chart
  latestOrders.value = orders
}

onMounted(() => void loadAll())
</script>
