<template>
  <div class="page home">
    <header class="hero">
      <div class="hero__top">
        <div>
          <div class="hero__name">{{ appStore.store?.storeName || '邻里小超' }}</div>
          <div class="hero__slogan">{{ appStore.store?.slogan || '新鲜到家，邻里共享' }}</div>
        </div>
        <div class="hero__badge">社区优选</div>
      </div>
      <div class="hero__search" @click="router.push('/products')">
        <span>搜索商品 / 分类</span>
      </div>
    </header>

    <div class="notice card">
      <span class="notice__label">公告</span>
      <span class="ellipsis">{{ appStore.store?.notice || '本店支持当日达' }}</span>
    </div>

    <div class="section-title">商品分类</div>
    <div class="card category-grid">
      <div v-for="category in categories" :key="category.id" class="category-grid__item"
           @click="router.push(`/products?categoryId=${category.id}`)">
        <img :src="category.icon" :alt="category.name" />
        <span>{{ category.name }}</span>
      </div>
    </div>

    <div class="section-title">热门推荐</div>
    <div class="product-grid">
      <ProductCard v-for="product in hotProducts" :key="product.id" :product="product" @add="addToCart" />
    </div>
    <EmptyState v-if="!loading && hotProducts.length === 0" text="暂无热门商品" />
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import TabBar from '@/components/TabBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCategories, getProducts } from '@/api/product'
import { useAppStore } from '@/store/app'
import { useCartStore } from '@/store/cart'
import { toast } from '@/utils/format'
import type { Category, ProductVO } from '@/types'

const router = useRouter()
const appStore = useAppStore()
const cartStore = useCartStore()
const categories = ref<Category[]>([])
const hotProducts = ref<ProductVO[]>([])
const loading = ref(true)

async function addToCart(product: ProductVO): Promise<void> {
  try {
    await cartStore.add(product.id, 1)
    toast('已加入购物车')
  } catch {
    // 错误已由 request 层提示
  }
}

onMounted(async () => {
  await appStore.loadStore()
  try {
    const [categoryResult, productResult] = await Promise.all([
      getCategories(),
      getProducts({ isHot: 1, status: 1, size: 20 }),
    ])
    categories.value = categoryResult
    hotProducts.value = productResult.records
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, var(--brand), #34c759);
  color: #fff;
  padding: 18px 14px 16px;
  border-radius: 0 0 18px 18px;
}

.hero__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.hero__name {
  font-size: 20px;
  font-weight: 700;
}

.hero__slogan {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 2px;
}

.hero__badge {
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
}

.hero__search {
  margin-top: 14px;
  background: rgba(255, 255, 255, 0.95);
  color: var(--text-sub);
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
}

.notice {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-sub);
}

.notice__label {
  flex: none;
  color: var(--brand);
  font-weight: 700;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 4px;
  text-align: center;
}

.category-grid__item {
  font-size: 12px;
}

.category-grid__item img {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  margin: 0 auto 6px;
  object-fit: cover;
  background: var(--brand-light);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 0 12px;
}
</style>
