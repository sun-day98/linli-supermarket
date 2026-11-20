<template>
  <div class="page plain">
    <header class="simple-header">
      <button class="back" @click="router.back()">‹</button>我的积分
    </header>
    <div class="points-hero">
      <div class="points-hero__value">{{ userStore.profile?.points ?? 0 }}</div>
      <div>当前可用积分 · 1 元 = 1 积分</div>
    </div>
    <div class="section-title">积分明细</div>
    <div v-for="record in records" :key="record.id" class="card record">
      <div class="row-between">
        <div class="record__source">{{ record.source }}</div>
        <div :class="['record__points', { minus: record.points < 0 }]">
          {{ record.points > 0 ? '+' : '' }}{{ record.points }}
        </div>
      </div>
      <div class="row-between muted">
        <span>{{ POINTS_TYPE_TEXT[record.type] || '变动' }}</span>
        <span>{{ formatDate(record.createdAt) }} · 余额 {{ record.balance }}</span>
      </div>
    </div>
    <EmptyState v-if="records.length === 0" text="暂无积分记录" />
    <div v-if="records.length < total" class="load-more" @click="loadMore">加载更多</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import { getPointsRecords } from '@/api/member'
import { useUserStore } from '@/store/user'
import { formatDate } from '@/utils/format'
import { POINTS_TYPE_TEXT } from '@/types'
import type { PointsRecord } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const records = ref<PointsRecord[]>([])
const total = ref(0)
const current = ref(1)

async function load(reset = true): Promise<void> {
  if (reset) current.value = 1
  const page = await getPointsRecords(current.value, 20)
  records.value = reset ? page.records : records.value.concat(page.records)
  total.value = page.total
}

function loadMore(): void {
  current.value += 1
  void load(false)
}

onMounted(async () => {
  await userStore.fetchProfile()
  await load()
})
</script>

<style scoped>
.simple-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 14px;
  background: #fff;
  font-size: 16px;
  font-weight: 700;
}

.back {
  border: none;
  background: transparent;
  font-size: 26px;
  line-height: 1;
}

.points-hero {
  background: linear-gradient(135deg, #ff976a, #f2270c);
  color: #fff;
  padding: 24px 16px;
  text-align: center;
  font-size: 12px;
}

.points-hero__value {
  font-size: 36px;
  font-weight: 700;
}

.record {
  padding: 12px 14px;
}

.record__source {
  font-size: 14px;
}

.record__points {
  color: var(--price);
  font-weight: 700;
}

.record__points.minus {
  color: var(--text-main);
}

.load-more {
  text-align: center;
  color: var(--text-sub);
  padding: 16px;
  font-size: 13px;
}
</style>
