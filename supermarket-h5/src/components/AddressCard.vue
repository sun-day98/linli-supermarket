<template>
  <div v-if="address" class="address-card card" @click="emit('select')">
    <div class="row-between">
      <div class="address-card__name">{{ address.receiver }}</div>
      <div class="muted">{{ address.phone }}</div>
    </div>
    <div class="address-card__detail">
      <span v-if="address.isDefault === 1" class="address-card__default">默认</span>
      {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}
    </div>
    <div v-if="selectable" class="address-card__hint">点击选择 / 更换地址 ›</div>
  </div>
  <div v-else class="card address-card address-card--empty" @click="emit('select')">
    + 请选择收货地址
  </div>
</template>

<script setup lang="ts">
import type { Address } from '@/types'

defineProps<{ address: Address | null; selectable?: boolean }>()
const emit = defineEmits<{ (e: 'select'): void }>()
</script>

<style scoped>
.address-card__name {
  font-size: 16px;
  font-weight: 700;
}

.address-card__detail {
  color: var(--text-sub);
  margin-top: 4px;
  font-size: 13px;
}

.address-card__default {
  display: inline-block;
  background: var(--brand-light);
  color: var(--brand);
  border-radius: 3px;
  padding: 0 5px;
  margin-right: 5px;
  font-size: 11px;
}

.address-card__hint {
  color: var(--brand);
  font-size: 12px;
  margin-top: 6px;
}

.address-card--empty {
  color: var(--text-sub);
  text-align: center;
  padding: 20px;
}
</style>
