<template>
  <div class="page detail">
    <template v-if="product">
      <div class="detail__hero">
        <img :src="product.image" :alt="product.name" />
      </div>
      <div class="card detail__price-card">
        <div class="price detail__price"><span class="price__symbol">¥</span>{{ product.price.toFixed(2) }}</div>
        <div class="detail__origin">¥{{ product.originalPrice.toFixed(2) }}</div>
        <div class="row-between">
          <div class="detail__name">{{ product.name }}</div>
          <div class="muted">已售 {{ product.sales }}</div>
        </div>
        <div class="muted">{{ product.subtitle }}</div>
        <div class="detail__meta">
          <span>规格：{{ product.spec }}</span>
          <span>库存：{{ product.stock }}{{ product.unit }}</span>
          <span>配送：预计当日达</span>
        </div>
      </div>

      <div class="card">
        <div class="section-title" style="margin-left: 0">商品详情</div>
        <p class="detail__text">{{ product.description }}</p>
        <div class="section-title" style="margin-left: 0">营养 / 成分信息</div>
        <p class="detail__text">{{ product.nutrition || '详见商品包装说明' }}</p>
      </div>

      <div class="card">
        <div class="row-between">
          <div class="section-title" style="margin: 0">用户评价（{{ reviews.length }}）</div>
          <span class="muted">仅展示审核通过</span>
        </div>
        <div v-for="review in reviews" :key="review.id" class="review">
          <div class="row-between">
            <span class="review__user">用户{{ String(review.userId).slice(-4) }}</span>
            <span class="review__rating">{{ '★'.repeat(review.rating) }}</span>
          </div>
          <div class="review__content">{{ review.content }}</div>
          <div class="muted">{{ formatDate(review.createdAt) }}</div>
        </div>
        <EmptyState v-if="reviews.length === 0" text="暂无评价" />
      </div>

      <SkuBar
        :price="product.price"
        :stock="product.stock"
        :stock-alert="product.stockAlert"
        :unit="product.unit"
        @add="handleAdd"
        @buy="handleBuy"
      />
    </template>
    <div v-else class="detail__loading">加载中...</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SkuBar from '@/components/SkuBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getProduct } from '@/api/product'
import { getReviews } from '@/api/review'
import { useCartStore } from '@/store/cart'
import { toast } from '@/utils/format'
import { formatDate } from '@/utils/format'
import type { ProductVO, Review } from '@/types'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const product = ref<ProductVO | null>(null)
const reviews = ref<Review[]>([])
const id = Number(route.params.id)

async function handleAdd(): Promise<void> {
  if (!product.value) return
  try {
    await cartStore.add(product.value.id, 1)
    toast('已加入购物车')
  } catch {
    // 已统一提示
  }
}

async function handleBuy(): Promise<void> {
  await handleAdd()
  void router.push('/checkout')
}

onMounted(async () => {
  product.value = await getProduct(id)
  const page = await getReviews(id, 1, 20)
  reviews.value = page.records
})
</script>

<style scoped>
.detail {
  padding-bottom: 76px;
}

.detail__hero img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  background: #f0f2f5;
}

.detail__price-card {
  margin-top: -10px;
  position: relative;
}

.detail__price {
  font-size: 24px;
}

.detail__origin {
  color: var(--text-light);
  text-decoration: line-through;
  font-size: 12px;
}

.detail__name {
  font-size: 16px;
  font-weight: 700;
}

.detail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  color: var(--text-sub);
  font-size: 12px;
  margin-top: 8px;
}

.detail__text {
  color: var(--text-sub);
  font-size: 13px;
  margin: 8px 0;
  white-space: pre-wrap;
}

.review {
  border-bottom: 1px solid var(--border);
  padding: 10px 0;
}

.review__user {
  font-size: 13px;
}

.review__rating {
  color: #ffb400;
  font-size: 13px;
}

.review__content {
  margin: 4px 0;
  font-size: 13px;
}

.detail__loading {
  padding: 60px;
  text-align: center;
  color: var(--text-sub);
}
</style>
