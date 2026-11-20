<template>
  <div class="coupon-picker card">
    <div class="row-between">
      <div class="section-title" style="margin: 0">优惠券</div>
      <span class="muted">{{ coupons.length }} 张可用</span>
    </div>
    <div class="coupon-picker__list">
      <div
        v-for="coupon in coupons"
        :key="coupon.userCouponId ?? coupon.id"
        class="coupon-picker__item"
        :class="{ 'coupon-picker__item--active': modelValue === coupon.userCouponId }"
        @click="choose(coupon)"
      >
        <div class="coupon-picker__amount">
          <span v-if="coupon.type === 2" class="price">{{ (coupon.discount * 10).toFixed(1) }}折</span>
          <span v-else class="price">¥{{ coupon.discount.toFixed(2) }}</span>
          <small>满{{ coupon.threshold.toFixed(0) }}可用</small>
        </div>
        <div class="coupon-picker__body">
          <div class="coupon-picker__name">{{ coupon.name }}</div>
          <div class="muted">{{ coupon.typeName }} · 单笔限用一张</div>
        </div>
        <div class="coupon-picker__radio" :class="{ 'coupon-picker__radio--on': modelValue === coupon.userCouponId }" />
      </div>
      <div class="coupon-picker__none" @click="choose(null)">
        不使用优惠券
        <span v-if="modelValue === null" class="coupon-picker__radio coupon-picker__radio--on" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CouponVO } from '@/types'

const props = defineProps<{ coupons: CouponVO[]; modelValue: number | null }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: number | null): void }>()

function choose(coupon: CouponVO | null): void {
  const next = coupon ? coupon.userCouponId : null
  if (next === props.modelValue) return
  emit('update:modelValue', next)
}
</script>

<style scoped>
.coupon-picker__list {
  margin-top: 8px;
}

.coupon-picker__item,
.coupon-picker__none {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 8px;
}

.coupon-picker__item--active {
  border-color: var(--brand);
  background: var(--brand-light);
}

.coupon-picker__amount {
  width: 84px;
  text-align: center;
  border-right: 1px dashed var(--border);
}

.coupon-picker__amount small {
  display: block;
  color: var(--text-sub);
  font-size: 10px;
}

.coupon-picker__body {
  flex: 1;
}

.coupon-picker__name {
  font-weight: 600;
  font-size: 13px;
}

.coupon-picker__radio {
  width: 18px;
  height: 18px;
  border: 1px solid var(--text-light);
  border-radius: 50%;
  flex: none;
}

.coupon-picker__radio--on {
  border-color: var(--brand);
  background: var(--brand);
  box-shadow: inset 0 0 0 4px #fff;
}

.coupon-picker__none {
  justify-content: space-between;
  color: var(--text-sub);
}
</style>
