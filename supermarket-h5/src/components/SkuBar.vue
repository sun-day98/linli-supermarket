<template>
  <div class="sku-bar">
    <div class="sku-bar__info">
      <div class="price"><span class="price__symbol">¥</span>{{ price.toFixed(2) }}</div>
      <div class="muted" :class="{ 'sku-bar__warn': stock <= stockAlert }">
        {{ stock <= stockAlert ? '库存紧张' : '库存充足' }} · 剩余 {{ stock }}{{ unit }}
      </div>
    </div>
    <div class="sku-bar__actions">
      <button class="btn btn--ghost" @click="emit('add')">加入购物车</button>
      <button class="btn" :disabled="stock <= 0" @click="emit('buy')">立即购买</button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  price: number
  stock: number
  stockAlert: number
  unit: string
}>()

const emit = defineEmits<{ (e: 'add'): void; (e: 'buy'): void }>()
</script>

<style scoped>
.sku-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--border);
  z-index: 60;
}

.sku-bar__info {
  flex: none;
}

.sku-bar__warn {
  color: var(--danger);
}

.sku-bar__actions {
  display: flex;
  gap: 8px;
}

.sku-bar__actions .btn {
  padding: 9px 14px;
}
</style>
