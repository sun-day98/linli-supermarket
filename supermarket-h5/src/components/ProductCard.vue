<template>
  <div class="product-card" @click="openDetail">
    <div class="product-card__image">
      <img :src="product.image" :alt="product.name" loading="lazy" />
      <span v-if="product.lowStock" class="product-card__tag">库存紧张</span>
    </div>
    <div class="product-card__body">
      <div class="product-card__name ellipsis">{{ product.name }}</div>
      <div class="product-card__sub ellipsis">{{ product.subtitle }}</div>
      <div class="row-between product-card__bottom">
        <div class="price">
          <span class="price__symbol">¥</span>{{ product.price.toFixed(2) }}
          <span class="product-card__origin">¥{{ product.originalPrice.toFixed(2) }}</span>
        </div>
        <button class="product-card__add" @click.stop="emit('add', product)">+</button>
      </div>
      <div class="muted">已售 {{ product.sales }} · {{ product.stock }}{{ product.unit }}可售</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { ProductVO } from '@/types'

const props = defineProps<{ product: ProductVO }>()
const emit = defineEmits<{ (e: 'add', product: ProductVO): void }>()
const router = useRouter()

function openDetail(): void {
  void router.push(`/products/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}

.product-card__image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: #f0f2f5;
}

.product-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-card__tag {
  position: absolute;
  left: 6px;
  top: 6px;
  background: rgba(245, 34, 45, 0.9);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.product-card__body {
  padding: 8px 10px 10px;
}

.product-card__name {
  font-size: 13px;
  font-weight: 600;
}

.product-card__sub {
  color: var(--text-sub);
  font-size: 11px;
  margin: 2px 0 6px;
}

.product-card__origin {
  color: var(--text-light);
  text-decoration: line-through;
  font-size: 10px;
  font-weight: 400;
  margin-left: 4px;
}

.product-card__add {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: var(--brand);
  color: #fff;
  font-size: 18px;
  line-height: 22px;
  padding: 0;
}

.product-card__bottom {
  margin-bottom: 4px;
}
</style>
