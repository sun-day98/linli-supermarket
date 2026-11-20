<template>
  <div class="page plain">
    <header class="simple-header">
      <button class="back" @click="router.back()">‹</button>我的优惠券
    </header>
    <div class="order-tabs">
      <span :class="{ active: tab === 'mine' }" @click="tab = 'mine'">我的券</span>
      <span :class="{ active: tab === 'center' }" @click="tab = 'center'">领券中心</span>
    </div>

    <template v-if="tab === 'mine'">
      <div v-for="coupon in mine" :key="coupon.userCouponId ?? coupon.id" class="coupon card"
           :class="{ 'coupon--used': coupon.userCouponStatus !== 0 }">
        <div class="coupon__left">
          <div class="coupon__amount">
            <template v-if="coupon.type === 2">{{ (coupon.discount * 10).toFixed(1) }}折</template>
            <template v-else><small>¥</small>{{ coupon.discount.toFixed(0) }}</template>
          </div>
          <div class="coupon__threshold">满{{ coupon.threshold.toFixed(0) }}元可用</div>
        </div>
        <div class="coupon__right">
          <div class="coupon__name">{{ coupon.name }}</div>
          <div class="muted">{{ coupon.typeName }} · {{ coupon.scope === 2 ? '新人专享' : '全场通用' }}</div>
          <div class="muted">{{ couponStatusText(coupon.userCouponStatus) }}</div>
          <div v-if="coupon.unusableReason && coupon.userCouponStatus === 0" class="coupon__reason">
            {{ coupon.unusableReason }}
          </div>
        </div>
      </div>
      <EmptyState v-if="mine.length === 0" text="暂无优惠券" />
    </template>

    <template v-else>
      <div v-for="coupon in definitions" :key="coupon.id" class="coupon card">
        <div class="coupon__left">
          <div class="coupon__amount">
            <template v-if="coupon.type === 2">{{ (coupon.discount * 10).toFixed(1) }}折</template>
            <template v-else><small>¥</small>{{ coupon.discount.toFixed(0) }}</template>
          </div>
          <div class="coupon__threshold">满{{ coupon.threshold.toFixed(0) }}元可用</div>
        </div>
        <div class="coupon__right">
          <div class="coupon__name">{{ coupon.name }}</div>
          <div class="muted">剩余 {{ Math.max(0, coupon.total - coupon.issued) }} 张</div>
          <button class="btn coupon__claim" @click="claim(coupon)">立即领取</button>
        </div>
      </div>
      <EmptyState v-if="definitions.length === 0" text="暂无可领取优惠券" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import { claimCoupon, getCouponDefinitions, getMyCoupons } from '@/api/coupon'
import { toast } from '@/utils/format'
import { couponStatusText } from '@/types'
import type { CouponVO } from '@/types'

const router = useRouter()
const tab = ref<'mine' | 'center'>('mine')
const mine = ref<CouponVO[]>([])
const definitions = ref<CouponVO[]>([])

async function loadMine(): Promise<void> {
  mine.value = await getMyCoupons()
}

async function loadDefinitions(): Promise<void> {
  definitions.value = await getCouponDefinitions()
}

async function claim(coupon: CouponVO): Promise<void> {
  try {
    await claimCoupon(coupon.id)
    toast('领取成功')
    await Promise.all([loadMine(), loadDefinitions()])
  } catch {
    // 已统一提示
  }
}

onMounted(async () => {
  await Promise.all([loadMine(), loadDefinitions()])
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

.order-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.order-tabs span {
  flex: 1;
  text-align: center;
  padding: 11px;
  font-size: 13px;
  color: var(--text-sub);
}

.order-tabs .active {
  color: var(--brand);
  font-weight: 700;
  border-bottom: 2px solid var(--brand);
}

.coupon {
  display: flex;
  padding: 0;
  overflow: hidden;
}

.coupon--used {
  opacity: 0.6;
}

.coupon__left {
  width: 96px;
  flex: none;
  background: var(--brand-light);
  color: var(--price);
  text-align: center;
  padding: 16px 4px;
  border-right: 1px dashed var(--brand);
}

.coupon__amount {
  font-size: 24px;
  font-weight: 700;
}

.coupon__amount small {
  font-size: 12px;
}

.coupon__threshold {
  font-size: 11px;
  color: var(--text-sub);
}

.coupon__right {
  flex: 1;
  min-width: 0;
  padding: 12px;
}

.coupon__name {
  font-weight: 700;
  font-size: 14px;
}

.coupon__reason {
  color: var(--danger);
  font-size: 11px;
  margin-top: 4px;
}

.coupon__claim {
  margin-top: 8px;
  padding: 4px 12px;
  font-size: 12px;
}
</style>
