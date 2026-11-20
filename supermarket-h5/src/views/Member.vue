<template>
  <div class="page member">
    <header class="member-header">
      <div class="member-header__avatar">{{ (userStore.profile?.nickname || '邻')[0] }}</div>
      <div>
        <div class="member-header__name" @click="ensureLogin">
          {{ userStore.profile?.nickname || '点击登录' }}
        </div>
        <div class="member-header__phone muted">{{ userStore.profile?.phone || '登录后享受完整服务' }}</div>
      </div>
    </header>

    <div class="card member-stats">
      <div class="member-stats__item" @click="go('/points')">
        <strong>{{ userStore.profile?.points ?? 0 }}</strong><span>积分</span>
      </div>
      <div class="member-stats__item" @click="go('/coupons')">
        <strong>{{ couponCount }}</strong><span>优惠券</span>
      </div>
      <div class="member-stats__item" @click="go('/orders')">
        <strong>{{ orderCount }}</strong><span>订单</span>
      </div>
    </div>

    <div class="card">
      <div class="section-title" style="margin-left: 0">我的订单</div>
      <div class="order-shortcuts">
        <div v-for="item in shortcuts" :key="item.status" @click="go(item.status === -1 ? '/orders' : `/orders?status=${item.status}`)">
          <div class="order-shortcuts__icon">{{ item.short }}</div>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </div>

    <div class="card member-menu">
      <div class="member-menu__item" @click="go('/points')"><span>积分明细</span><span>›</span></div>
      <div class="member-menu__item" @click="go('/coupons')"><span>我的优惠券</span><span>›</span></div>
      <div class="member-menu__item" @click="go('/address')"><span>收货地址</span><span>›</span></div>
      <div class="member-menu__item" @click="go('/service')"><span>联系客服</span><span>›</span></div>
      <div class="member-menu__item" @click="logout"><span>退出登录</span><span>›</span></div>
    </div>
    <TabBar />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import { getMyCoupons } from '@/api/coupon'
import { getOrders } from '@/api/order'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const couponCount = ref(0)
const orderCount = ref(0)
const shortcuts = [
  { label: '全部订单', status: -1, short: '全' },
  { label: '待付款', status: 0, short: '付' },
  { label: '待配送', status: 1, short: '配' },
  { label: '配送中', status: 2, short: '送' },
  { label: '已完成', status: 3, short: '完' },
]

function go(path: string): void {
  void router.push(path)
}

function ensureLogin(): void {
  if (!userStore.isLogin) void router.push('/login')
}

function logout(): void {
  userStore.logout()
  cartStore.clearLocal()
  void router.push('/')
}

onMounted(async () => {
  if (!userStore.isLogin) return
  await userStore.fetchProfile()
  const [coupons, orders] = await Promise.all([
    getMyCoupons(),
    getOrders({ current: 1, size: 1 }),
  ])
  couponCount.value = coupons.length
  orderCount.value = orders.total
})
</script>

<style scoped>
.member-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  background: linear-gradient(135deg, var(--brand), #34c759);
  color: #fff;
}

.member-header__avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.member-header__name {
  font-size: 18px;
  font-weight: 700;
}

.member-header__phone {
  color: rgba(255, 255, 255, 0.85);
}

.member-stats {
  display: flex;
  text-align: center;
  margin-top: -14px;
  position: relative;
}

.member-stats__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.member-stats__item strong {
  font-size: 18px;
  color: var(--price);
}

.member-stats__item span {
  font-size: 12px;
  color: var(--text-sub);
}

.order-shortcuts {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  text-align: center;
}

.order-shortcuts__icon {
  width: 34px;
  height: 34px;
  margin: 0 auto 4px;
  border-radius: 50%;
  background: var(--brand-light);
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.member-menu__item {
  display: flex;
  justify-content: space-between;
  padding: 12px 2px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}

.member-menu__item:last-child {
  border-bottom: none;
}
</style>
