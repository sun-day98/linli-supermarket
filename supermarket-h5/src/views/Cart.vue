<template>
  <div class="page cart">
    <header class="cart-header">购物车 <span class="muted">({{ cartStore.items.length }})</span></header>

    <template v-if="cartStore.items.length > 0">
      <div v-for="item in cartStore.items" :key="item.id" class="cart-item card">
        <div class="cart-item__check" :class="{ on: item.selected === 1 }" @click="toggle(item)">
          <span v-if="item.selected === 1">✓</span>
        </div>
        <img :src="item.productImage" :alt="item.productName" />
        <div class="cart-item__body">
          <div class="ellipsis cart-item__name">{{ item.productName }}</div>
          <div class="muted ellipsis">{{ item.spec }}</div>
          <div class="row-between cart-item__bottom">
            <div class="price">¥{{ item.price.toFixed(2) }}</div>
            <div class="stepper">
              <button @click="changeQty(item, item.quantity - 1)">-</button>
              <span>{{ item.quantity }}</span>
              <button @click="changeQty(item, item.quantity + 1)">+</button>
            </div>
          </div>
        </div>
        <button class="cart-item__remove" @click="remove(item.id)">×</button>
      </div>
      <div class="cart-tip muted" v-if="cartStore.items.some((item) => item.quantity >= item.stock)">
        部分商品已达库存上限
      </div>
    </template>
    <EmptyState v-else text="购物车还是空的，去逛逛吧">
      <button class="btn" @click="router.push('/')">去首页</button>
    </EmptyState>

    <div class="cart-bar">
      <div class="cart-bar__check" :class="{ on: cartStore.allSelected }" @click="toggleAll">
        <span v-if="cartStore.allSelected">✓</span>
      </div>
      <span @click="toggleAll">全选</span>
      <div class="cart-bar__total">
        <div>合计 <span class="price">¥{{ cartStore.totalAmount.toFixed(2) }}</span></div>
        <div class="muted">已选 {{ cartStore.totalCount }} 件</div>
      </div>
      <button class="btn" :disabled="cartStore.totalCount === 0" @click="router.push('/checkout')">
        去结算
      </button>
    </div>
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { useCartStore } from '@/store/cart'
import type { CartVO } from '@/types'

const router = useRouter()
const cartStore = useCartStore()

async function toggle(item: CartVO): Promise<void> {
  await cartStore.toggleSelect(item.id, item.productId, item.selected !== 1)
}

async function changeQty(item: CartVO, quantity: number): Promise<void> {
  if (quantity < 1 || quantity > item.stock) return
  await cartStore.updateQty(item.id, item.productId, quantity)
}

async function remove(id: number): Promise<void> {
  await cartStore.remove(id)
}

async function toggleAll(): Promise<void> {
  const target = !cartStore.allSelected
  for (const item of cartStore.items) {
    if ((item.selected === 1) !== target) {
      await cartStore.toggleSelect(item.id, item.productId, target)
    }
  }
}

onMounted(() => void cartStore.sync())
</script>

<style scoped>
.cart-header {
  padding: 14px;
  font-size: 16px;
  font-weight: 700;
  background: #fff;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
}

.cart-item__check,
.cart-bar__check {
  width: 18px;
  height: 18px;
  border: 1px solid var(--text-light);
  border-radius: 50%;
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
}

.cart-item__check.on,
.cart-bar__check.on {
  background: var(--brand);
  border-color: var(--brand);
}

.cart-item img {
  width: 66px;
  height: 66px;
  border-radius: 6px;
  object-fit: cover;
  flex: none;
}

.cart-item__body {
  flex: 1;
  min-width: 0;
}

.cart-item__name {
  font-size: 13px;
  font-weight: 600;
}

.cart-item__bottom {
  margin-top: 6px;
}

.cart-item__remove {
  align-self: flex-start;
  border: none;
  background: transparent;
  color: var(--text-light);
  font-size: 20px;
  line-height: 1;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stepper button {
  width: 22px;
  height: 22px;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: 4px;
  line-height: 1;
}

.cart-tip {
  text-align: center;
}

.cart-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: var(--tabbar-height);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid var(--border);
  z-index: 40;
  font-size: 13px;
}

.cart-bar__total {
  flex: 1;
  text-align: right;
}
</style>
