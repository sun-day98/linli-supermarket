<template>
  <div class="amount-panel card">
    <div class="section-title" style="margin-left: 0">金额明细</div>
    <div class="row-between amount-panel__line">
      <span>商品金额</span>
      <span>¥{{ preview?.goodsAmount?.toFixed(2) ?? '0.00' }}</span>
    </div>
    <div class="row-between amount-panel__line">
      <span>配送费</span>
      <span>
        <span v-if="preview?.freeDelivery" class="amount-panel__free">已免配送费</span>
        ¥{{ preview?.deliveryFee?.toFixed(2) ?? '0.00' }}
      </span>
    </div>
    <div class="row-between amount-panel__line">
      <span>优惠金额</span>
      <span class="price">-¥{{ preview?.discountAmount?.toFixed(2) ?? '0.00' }}</span>
    </div>
    <div class="row-between amount-panel__total">
      <span>应付金额</span>
      <span class="price">¥{{ preview?.payAmount?.toFixed(2) ?? '0.00' }}</span>
    </div>
    <div v-if="preview && !preview.deliverable" class="amount-panel__error">
      当前地址暂不支持配送，请更换收货地址
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CheckoutPreviewVO } from '@/types'

defineProps<{ preview: CheckoutPreviewVO | null }>()
</script>

<style scoped>
.amount-panel__line {
  padding: 5px 0;
  color: var(--text-sub);
}

.amount-panel__free {
  color: var(--brand);
  font-size: 11px;
  margin-right: 6px;
}

.amount-panel__total {
  border-top: 1px dashed var(--border);
  margin-top: 8px;
  padding-top: 10px;
  font-weight: 700;
}

.amount-panel__error {
  margin-top: 8px;
  color: var(--danger);
  font-size: 12px;
}
</style>
