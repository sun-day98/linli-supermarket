<template>
  <div class="page orders">
    <header class="orders-header">我的订单</header>
    <div class="order-tabs">
      <span v-for="tab in tabs" :key="tab.value" :class="{ active: status === tab.value }"
            @click="changeTab(tab.value)">{{ tab.label }}</span>
    </div>

    <div v-for="order in orders" :key="order.id" class="card order-card" @click="openDetail(order.id)">
      <div class="row-between order-card__head">
        <span class="muted">{{ formatDate(order.createdAt) }}</span>
        <span :class="['order-card__status', `s${order.status}`]">{{ order.statusText }}</span>
      </div>
      <div class="order-card__items">
        <img v-for="item in order.items.slice(0, 3)" :key="item.id" :src="item.productImage" :alt="item.productName" />
        <div v-if="order.items.length > 3" class="order-card__more">+{{ order.items.length - 3 }}</div>
      </div>
      <div class="row-between order-card__foot">
        <span class="muted">共 {{ order.items.length }} 种商品</span>
        <span>实付 <span class="price">¥{{ order.payAmount.toFixed(2) }}</span></span>
      </div>
      <div class="order-card__actions" @click.stop>
        <button v-if="order.status === 0" class="btn btn--danger" @click="cancel(order.id)">取消订单</button>
        <button v-if="order.status === 0" class="btn" @click="openDetail(order.id)">去支付</button>
        <button v-if="order.status === 1" class="btn btn--ghost" @click="cancel(order.id)">取消订单</button>
        <button v-if="order.status === 2" class="btn" @click="finish(order.id)">确认收货</button>
      </div>
    </div>
    <EmptyState v-if="!loading && orders.length === 0" text="暂无相关订单">
      <button class="btn" @click="router.push('/')">去逛逛</button>
    </EmptyState>
    <div v-if="orders.length < total" class="load-more" @click="loadMore">加载更多</div>
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { cancelOrder, finishOrder, getOrders } from '@/api/order'
import { toast, formatDate } from '@/utils/format'
import type { OrderVO } from '@/types'

const router = useRouter()
const tabs = [
  { label: '全部', value: -1 },
  { label: '待付款', value: 0 },
  { label: '待配送', value: 1 },
  { label: '配送中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
]
const status = ref<number>(-1)
const orders = ref<OrderVO[]>([])
const total = ref(0)
const current = ref(1)
const loading = ref(true)

async function load(reset = true): Promise<void> {
  loading.value = true
  if (reset) current.value = 1
  try {
    const params: Record<string, unknown> = { current: current.value, size: 10 }
    if (status.value >= 0) params.status = status.value
    const page = await getOrders(params)
    orders.value = reset ? page.records : orders.value.concat(page.records)
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function changeTab(value: number): void {
  status.value = value
  void load()
}

function loadMore(): void {
  current.value += 1
  void load(false)
}

function openDetail(id: number): void {
  void router.push(`/orders/${id}`)
}

async function cancel(id: number): Promise<void> {
  await cancelOrder(id)
  toast('订单已取消')
  await load()
}

async function finish(id: number): Promise<void> {
  await finishOrder(id)
  toast('确认收货成功，积分已发放')
  await load()
}

onMounted(() => void load())
</script>

<style scoped>
.orders-header {
  padding: 14px;
  background: #fff;
  font-size: 16px;
  font-weight: 700;
}

.order-tabs {
  display: flex;
  overflow-x: auto;
  background: #fff;
  border-bottom: 1px solid var(--border);
  padding: 0 8px;
}

.order-tabs span {
  flex: none;
  padding: 10px 10px;
  font-size: 13px;
  color: var(--text-sub);
}

.order-tabs .active {
  color: var(--brand);
  font-weight: 700;
  border-bottom: 2px solid var(--brand);
}

.order-card__head {
  font-size: 12px;
}

.order-card__status {
  color: var(--brand);
  font-weight: 700;
}

.order-card__status.s0 {
  color: var(--warning);
}

.order-card__status.s4 {
  color: var(--text-light);
}

.order-card__items {
  display: flex;
  gap: 6px;
  margin: 10px 0;
}

.order-card__items img {
  width: 54px;
  height: 54px;
  border-radius: 6px;
  object-fit: cover;
}

.order-card__more {
  width: 54px;
  height: 54px;
  border-radius: 6px;
  background: var(--bg-page);
  color: var(--text-sub);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.order-card__foot {
  font-size: 13px;
}

.order-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.order-card__actions .btn {
  padding: 5px 12px;
  font-size: 12px;
}

.load-more {
  text-align: center;
  color: var(--text-sub);
  padding: 16px;
  font-size: 13px;
}
</style>
