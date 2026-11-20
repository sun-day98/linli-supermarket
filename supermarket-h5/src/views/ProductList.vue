<template>
  <div class="page">
    <header class="list-header">
      <button class="list-header__back" @click="router.back()">‹</button>
      <input v-model="keywordInput" placeholder="搜索商品" @keyup.enter="doSearch" />
      <button class="list-header__search" @click="doSearch">搜索</button>
    </header>

    <div class="filter-bar">
      <span :class="{ active: !isHotOnly }" @click="setHot(false)">全部</span>
      <span :class="{ active: isHotOnly }" @click="setHot(true)">热门</span>
      <span class="filter-bar__spacer" />
      <span class="muted">共 {{ total }} 件</span>
    </div>

    <div class="product-grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" @add="addToCart" />
    </div>
    <EmptyState v-if="!loading && products.length === 0" text="没有找到相关商品">
      <button class="btn btn--ghost" @click="router.push('/')">返回首页</button>
    </EmptyState>
    <div v-if="products.length < total" class="load-more" @click="loadMore">加载更多</div>
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import TabBar from '@/components/TabBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getProducts } from '@/api/product'
import { useCartStore } from '@/store/cart'
import { toast } from '@/utils/format'
import type { ProductVO } from '@/types'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const products = ref<ProductVO[]>([])
const total = ref(0)
const current = ref(1)
const loading = ref(true)
const isHotOnly = ref(false)
const keywordInput = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

async function load(reset = true): Promise<void> {
  loading.value = true
  if (reset) current.value = 1
  try {
    const params: Record<string, unknown> = {
      status: 1,
      current: current.value,
      size: 12,
    }
    if (route.query.categoryId) params.categoryId = Number(route.query.categoryId)
    if (route.query.keyword) params.keyword = String(route.query.keyword)
    if (isHotOnly.value) params.isHot = 1
    const page = await getProducts(params)
    products.value = reset ? page.records : products.value.concat(page.records)
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function loadMore(): void {
  current.value += 1
  void load(false)
}

function setHot(hot: boolean): void {
  isHotOnly.value = hot
  void load()
}

function doSearch(): void {
  void router.push({ path: '/products', query: keywordInput.value ? { keyword: keywordInput.value } : {} })
}

async function addToCart(product: ProductVO): Promise<void> {
  try {
    await cartStore.add(product.id, 1)
    toast('已加入购物车')
  } catch {
    // 已统一提示
  }
}

watch(() => route.fullPath, () => {
  keywordInput.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  void load()
})

onMounted(() => void load())
</script>

<style scoped>
.list-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fff;
}

.list-header__back {
  border: none;
  background: transparent;
  font-size: 26px;
  line-height: 1;
  padding: 0 4px;
}

.list-header input {
  flex: 1;
  border: 1px solid var(--border);
  background: var(--bg-page);
  border-radius: 999px;
  padding: 7px 12px;
  outline: none;
  font-size: 13px;
}

.list-header__search {
  border: none;
  background: var(--brand);
  color: #fff;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 13px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 14px;
  font-size: 13px;
}

.filter-bar .active {
  color: var(--brand);
  font-weight: 700;
}

.filter-bar__spacer {
  flex: 1;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 0 12px;
}

.load-more {
  text-align: center;
  color: var(--text-sub);
  font-size: 13px;
  padding: 16px;
}
</style>
