<template>
  <div class="page plain">
    <header class="simple-header">
      <button class="back" @click="router.back()">‹</button>客服中心
    </header>

    <div class="card service-hero">
      <div class="service-hero__icon">客</div>
      <div class="service-hero__name">{{ store?.storeName || '邻里小超' }}</div>
      <div class="muted">{{ store?.slogan }}</div>
    </div>

    <div class="card">
      <div class="section-title" style="margin-left: 0">客服热线</div>
      <div class="service-phone">{{ store?.servicePhone || '400-800-1234' }}</div>
      <button class="btn service-copy" @click="copyPhone">复制号码</button>
      <div class="muted">服务时间：每日 08:00 - 21:00</div>
    </div>

    <div class="card">
      <div class="section-title" style="margin-left: 0">门店公告</div>
      <p class="service-text">{{ store?.notice || '本店支持当日达，满59元免配送费' }}</p>
      <div class="row-between service-line">
        <span>配送费</span><span class="price">¥{{ (store?.deliveryFee ?? 5).toFixed(2) }}</span>
      </div>
      <div class="row-between service-line">
        <span>免配送门槛</span><span class="price">¥{{ (store?.freeDeliveryThreshold ?? 59).toFixed(2) }}</span>
      </div>
    </div>

    <div class="card">
      <div class="section-title" style="margin-left: 0">常见问题</div>
      <div class="faq">
        <div class="faq__q">Q：如何获得积分？</div>
        <div class="faq__a">A：确认收货后按实付金额发放，1 元 = 1 积分。</div>
        <div class="faq__q">Q：优惠券可以叠加吗？</div>
        <div class="faq__a">A：单笔订单限用一张，新人券不与其他券叠加。</div>
        <div class="faq__q">Q：取消订单后库存会退回吗？</div>
        <div class="faq__a">A：会。取消后库存、优惠券与积分均自动回滚。</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { toast } from '@/utils/format'
import type { StoreSetting } from '@/types'

const router = useRouter()
const appStore = useAppStore()
const store = ref<StoreSetting | null>(null)

async function copyPhone(): Promise<void> {
  const phone = store.value?.servicePhone || '400-800-1234'
  try {
    await navigator.clipboard.writeText(phone)
    toast('号码已复制')
  } catch {
    toast(`请手动复制：${phone}`)
  }
}

onMounted(async () => {
  store.value = await appStore.loadStore()
})
</script>

<style scoped>
.simple-header {
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

.service-hero {
  text-align: center;
}

.service-hero__icon {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: var(--brand-light);
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 8px;
}

.service-hero__name {
  font-size: 16px;
  font-weight: 700;
}

.service-phone {
  font-size: 22px;
  font-weight: 700;
  color: var(--brand);
  text-align: center;
  margin: 10px 0;
}

.service-copy {
  display: block;
  margin: 0 auto 10px;
}

.service-text,
.faq__a,
.faq__q {
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.7;
}

.faq__q {
  color: var(--text-main);
  font-weight: 600;
  margin-top: 8px;
}

.service-line {
  padding: 4px 0;
  font-size: 13px;
}
</style>
