<template>
  <div class="page plain">
    <header class="detail-header">
      <button class="back" @click="router.back()">‹</button>
      订单详情
    </header>

    <template v-if="order">
      <div class="card status-card">
        <div class="status-card__title">{{ order.statusText }}</div>
        <div class="muted" v-if="order.status === 2">配送员已在途中，请耐心等待</div>
        <div class="muted" v-else-if="order.status === 0">请在 30 分钟内完成支付</div>
      </div>

      <div class="card">
        <div class="row-between">
          <div>收货人 <strong>{{ order.receiver }}</strong></div>
          <div class="muted">{{ order.phone }}</div>
        </div>
        <div class="muted address-line">{{ order.addressDetail }}</div>
      </div>

      <div class="card">
        <div class="section-title" style="margin-left: 0">商品信息</div>
        <div v-for="item in order.items" :key="item.id" class="detail-item">
          <img :src="item.productImage" :alt="item.productName" />
          <div class="detail-item__body">
            <div class="ellipsis">{{ item.productName }}</div>
            <div class="muted">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</div>
          </div>
          <div class="price">¥{{ item.totalAmount.toFixed(2) }}</div>
        </div>
        <div class="amount-line"><span>商品金额</span><span>¥{{ order.goodsAmount.toFixed(2) }}</span></div>
        <div class="amount-line"><span>配送费</span><span>¥{{ order.deliveryFee.toFixed(2) }}</span></div>
        <div class="amount-line"><span>优惠</span><span class="price">-¥{{ order.discountAmount.toFixed(2) }}</span></div>
        <div class="amount-line amount-line--total"><span>实付款</span><span class="price">¥{{ order.payAmount.toFixed(2) }}</span></div>
      </div>

      <div class="card">
        <div class="info-line"><span>订单编号</span><span>{{ order.orderNo }}</span></div>
        <div class="info-line"><span>下单时间</span><span>{{ formatDate(order.createdAt) }}</span></div>
        <div class="info-line" v-if="order.payTime"><span>支付时间</span><span>{{ formatDate(order.payTime) }}</span></div>
        <div class="info-line" v-if="order.finishTime"><span>完成时间</span><span>{{ formatDate(order.finishTime) }}</span></div>
        <div class="info-line" v-if="order.remark"><span>备注</span><span>{{ order.remark }}</span></div>
      </div>

      <div class="detail-actions">
        <button v-if="order.status === 0" class="btn btn--danger" @click="cancel">取消订单</button>
        <button v-if="order.status === 0" class="btn" @click="paySheet = true">立即支付</button>
        <button v-if="order.status === 1" class="btn btn--ghost" @click="cancel">取消订单</button>
        <button v-if="order.status === 2" class="btn" @click="finish">确认收货</button>
      </div>
    </template>
    <div v-else class="detail__loading">加载中...</div>

    <div v-if="paySheet" class="mask" @click.self="paySheet = false">
      <div class="sheet">
        <div class="sheet__title">选择支付方式</div>
        <div class="pay-option" @click="pay('WECHAT')">
          <span class="pay-option__icon wechat">微</span>
          <span>微信支付（模拟）</span>
          <span>›</span>
        </div>
        <div class="pay-option" @click="pay('ALIPAY')">
          <span class="pay-option__icon alipay">支</span>
          <span>支付宝（模拟）</span>
          <span>›</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cancelOrder, finishOrder, getOrder, payOrder } from '@/api/order'
import { toast, formatDate } from '@/utils/format'
import type { OrderVO } from '@/types'

const route = useRoute()
const router = useRouter()
const order = ref<OrderVO | null>(null)
const paySheet = ref(false)
const id = Number(route.params.id)

async function load(): Promise<void> {
  order.value = await getOrder(id)
}

async function pay(payType: 'WECHAT' | 'ALIPAY'): Promise<void> {
  await payOrder(id, payType)
  paySheet.value = false
  toast('支付成功')
  await load()
}

async function cancel(): Promise<void> {
  await cancelOrder(id)
  toast('订单已取消')
  await load()
}

async function finish(): Promise<void> {
  await finishOrder(id)
  toast('确认收货成功，积分已发放')
  await load()
}

onMounted(async () => {
  await load()
  if (route.query.pay === '1' && order.value?.status === 0) paySheet.value = true
})
</script>

<style scoped>
.plain {
  padding-bottom: 30px;
}

.detail-header {
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

.status-card {
  background: linear-gradient(135deg, var(--brand), #34c759);
  color: #fff;
}

.status-card__title {
  font-size: 19px;
  font-weight: 700;
}

.address-line {
  margin-top: 6px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.detail-item img {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  object-fit: cover;
}

.detail-item__body {
  flex: 1;
  min-width: 0;
  font-size: 13px;
}

.amount-line,
.info-line {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
  color: var(--text-sub);
}

.amount-line--total {
  border-top: 1px dashed var(--border);
  margin-top: 8px;
  padding-top: 10px;
  color: var(--text-main);
  font-weight: 700;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 6px 14px;
}

.detail__loading {
  padding: 60px;
  text-align: center;
  color: var(--text-sub);
}

.pay-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 6px;
  border-bottom: 1px solid var(--border);
}

.pay-option > span:nth-child(2) {
  flex: 1;
}

.pay-option__icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.pay-option__icon.wechat {
  background: #09bb07;
}

.pay-option__icon.alipay {
  background: #1677ff;
}
</style>
