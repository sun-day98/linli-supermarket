<template>
  <div class="page checkout">
    <header class="checkout-header">
      <button class="back" @click="router.back()">‹</button>
      确认订单
    </header>

    <AddressCard :address="currentAddress" selectable @select="addressSheet = true" />

    <div class="card">
      <div class="section-title" style="margin-left: 0">商品清单</div>
      <div v-for="item in cartStore.selectedItems" :key="item.id" class="checkout-item">
        <img :src="item.productImage" :alt="item.productName" />
        <div class="checkout-item__body">
          <div class="ellipsis">{{ item.productName }}</div>
          <div class="muted">{{ item.spec }} × {{ item.quantity }}</div>
        </div>
        <div class="price">¥{{ item.subtotal.toFixed(2) }}</div>
      </div>
      <EmptyState v-if="cartStore.selectedItems.length === 0" text="没有勾选的商品" />
    </div>

    <CouponPicker v-model="couponId" :coupons="coupons" />

    <div class="card">
      <div class="field">
        <label>订单备注</label>
        <input v-model="remark" placeholder="选填，如送货时间" @blur="refreshPreview" />
      </div>
    </div>

    <AmountPanel :preview="preview" />

    <div class="checkout-bar">
      <div>
        <div>应付 <span class="price">¥{{ preview?.payAmount?.toFixed(2) ?? '0.00' }}</span></div>
        <div class="muted">共 {{ cartStore.totalCount }} 件商品</div>
      </div>
      <button class="btn" :disabled="submitting || cartStore.totalCount === 0" @click="submit">提交订单</button>
    </div>

    <div v-if="addressSheet" class="mask" @click.self="addressSheet = false">
      <div class="sheet">
        <div class="sheet__title">选择收货地址</div>
        <div v-for="address in addresses" :key="address.id" class="sheet-address" @click="chooseAddress(address.id)">
          <div class="row-between">
            <strong>{{ address.receiver }}</strong>
            <span class="muted">{{ address.phone }}</span>
          </div>
          <div class="muted">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</div>
        </div>
        <button class="btn btn--ghost sheet__add" @click="router.push('/address')">管理 / 新增地址</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AddressCard from '@/components/AddressCard.vue'
import CouponPicker from '@/components/CouponPicker.vue'
import AmountPanel from '@/components/AmountPanel.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getAddresses } from '@/api/address'
import { getAvailableCoupons } from '@/api/coupon'
import { createOrder, previewOrder } from '@/api/order'
import { useCartStore } from '@/store/cart'
import { toast } from '@/utils/format'
import type { Address, CheckoutDTO, CheckoutPreviewVO, CouponVO } from '@/types'

const router = useRouter()
const cartStore = useCartStore()
const addresses = ref<Address[]>([])
const addressId = ref<number | null>(null)
const coupons = ref<CouponVO[]>([])
const couponId = ref<number | null>(null)
const preview = ref<CheckoutPreviewVO | null>(null)
const remark = ref('')
const addressSheet = ref(false)
const submitting = ref(false)

const currentAddress = computed(() => addresses.value.find((item) => item.id === addressId.value) ?? null)

function buildDTO(): CheckoutDTO {
  return {
    items: cartStore.selectedItems.map((item) => ({ productId: item.productId, quantity: item.quantity })),
    addressId: addressId.value ?? 0,
    couponId: couponId.value,
    remark: remark.value,
  }
}

async function refreshPreview(): Promise<void> {
  if (!addressId.value || cartStore.selectedItems.length === 0) {
    preview.value = null
    return
  }
  try {
    preview.value = await previewOrder(buildDTO())
  } catch {
    preview.value = null
    couponId.value = null
  }
}

function chooseAddress(id: number): void {
  addressId.value = id
  addressSheet.value = false
}

async function submit(): Promise<void> {
  if (!addressId.value) {
    toast('请选择收货地址')
    return
  }
  if (preview.value && !preview.value.deliverable) {
    toast('当前地址暂不支持配送')
    return
  }
  submitting.value = true
  try {
    const dto = buildDTO()
    if (preview.value) dto.expectedPayAmount = preview.value.payAmount
    const order = await createOrder(dto)
    await cartStore.sync()
    void router.replace(`/orders/${order.id}?pay=1`)
  } finally {
    submitting.value = false
  }
}

watch([addressId, couponId], () => void refreshPreview())

onMounted(async () => {
  await cartStore.sync()
  if (cartStore.selectedItems.length === 0) {
    void router.replace('/cart')
    return
  }
  addresses.value = await getAddresses()
  const preferred = addresses.value.find((item) => item.isDefault === 1) ?? addresses.value[0]
  if (preferred) addressId.value = preferred.id
  coupons.value = await getAvailableCoupons(cartStore.totalAmount)
  await refreshPreview()
})
</script>

<style scoped>
.checkout-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 14px;
  background: #fff;
  font-size: 16px;
  font-weight: 700;
}

.back {
  border: none;
  background: transparent;
  font-size: 26px;
  line-height: 1;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.checkout-item img {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  object-fit: cover;
}

.checkout-item__body {
  flex: 1;
  min-width: 0;
  font-size: 13px;
}

.checkout-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--border);
  z-index: 60;
  font-size: 13px;
}

.sheet-address {
  border-bottom: 1px solid var(--border);
  padding: 12px 4px;
  font-size: 13px;
}

.sheet__add {
  width: 100%;
  margin-top: 12px;
}
</style>
