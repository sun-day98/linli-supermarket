<template>
  <div class="page">
    <div class="cat-header">商品分类</div>
    <div class="cat-layout">
      <aside class="cat-menu">
        <div v-for="category in categories" :key="category.id"
             :class="['cat-menu__item', { 'cat-menu__item--active': current === category.id }]"
             @click="current = category.id">
          {{ category.name }}
        </div>
      </aside>
      <section class="cat-content">
        <div v-if="currentCategory" class="cat-content__banner">
          <img :src="currentCategory.icon" :alt="currentCategory.name" />
          <div>
            <div class="cat-content__title">{{ currentCategory.name }}</div>
            <div class="muted">精选好物 · 新鲜直达</div>
          </div>
        </div>
        <div class="cat-content__list">
          <div v-for="product in products" :key="product.id" class="cat-product" @click="openDetail(product.id)">
            <img :src="product.image" :alt="product.name" />
            <div class="cat-product__body">
              <div class="ellipsis">{{ product.name }}</div>
              <div class="muted ellipsis">{{ product.subtitle }}</div>
              <div class="price">¥{{ product.price.toFixed(2) }}</div>
            </div>
          </div>
          <EmptyState v-if="products.length === 0" text="该分类暂无商品" />
        </div>
      </section>
    </div>
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCategories, getProducts } from '@/api/product'
import type { Category, ProductVO } from '@/types'

const router = useRouter()
const categories = ref<Category[]>([])
const products = ref<ProductVO[]>([])
const current = ref<number>(0)

const currentCategory = computed(() => categories.value.find((item) => item.id === current.value) ?? null)

function openDetail(id: number): void {
  void router.push(`/products/${id}`)
}

onMounted(async () => {
  categories.value = await getCategories()
  if (categories.value.length > 0) current.value = categories.value[0].id
})

watch(current, async (id) => {
  if (!id) return
  const page = await getProducts({ categoryId: id, status: 1, size: 50 })
  products.value = page.records
}, { immediate: true })
</script>

<style scoped>
.cat-header {
  background: var(--brand);
  color: #fff;
  padding: 14px;
  font-size: 16px;
  font-weight: 700;
}

.cat-layout {
  display: flex;
  align-items: stretch;
  min-height: calc(100vh - var(--tabbar-height) - 46px);
}

.cat-menu {
  width: 92px;
  flex: none;
  background: #f7f8fa;
}

.cat-menu__item {
  padding: 14px 6px;
  text-align: center;
  font-size: 12px;
  color: var(--text-sub);
  border-left: 3px solid transparent;
}

.cat-menu__item--active {
  background: #fff;
  color: var(--brand);
  font-weight: 700;
  border-left-color: var(--brand);
}

.cat-content {
  flex: 1;
  min-width: 0;
  padding: 10px;
}

.cat-content__banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--brand-light);
  border-radius: var(--radius);
  padding: 10px;
}

.cat-content__banner img {
  width: 48px;
  height: 48px;
  border-radius: 10px;
}

.cat-content__title {
  font-weight: 700;
  font-size: 15px;
}

.cat-product {
  display: flex;
  gap: 10px;
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  margin-top: 8px;
}

.cat-product img {
  width: 68px;
  height: 68px;
  border-radius: 6px;
  object-fit: cover;
  flex: none;
}

.cat-product__body {
  min-width: 0;
  flex: 1;
}

.cat-product__body > div:first-child {
  font-weight: 600;
  font-size: 13px;
}
</style>
