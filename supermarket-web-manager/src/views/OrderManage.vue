<template>
  <div>
    <PageHeader title="订单管理" description="订单状态流转与履约处理">
      <el-button @click="load">刷新</el-button>
    </PageHeader>

    <div class="table-toolbar">
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px" @change="reload">
        <el-option v-for="(label, value) in ORDER_STATUS_TEXT" :key="value" :label="label" :value="Number(value)" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="订单号 / 收货人 / 手机号" clearable style="width: 240px"
                @keyup.enter="reload" />
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" size="small" stripe border>
      <el-table-column prop="orderNo" label="订单号" min-width="195" />
      <el-table-column label="收货人" width="140">
        <template #default="{ row }">{{ row.receiver }}<div class="muted">{{ row.phone }}</div></template>
      </el-table-column>
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">{{ row.items.map((i: OrderItemVO) => i.productName).join('、') }}</template>
      </el-table-column>
      <el-table-column label="实付" width="100">
        <template #default="{ row }"><span class="money">¥{{ row.payAmount.toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="ORDER_STATUS_TAG[row.status] || 'info'" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" min-width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 0" link type="success" @click="handlePay(row)">模拟收款</el-button>
          <el-button v-if="row.status === 1" link type="primary" @click="openDeliver(row)">发货</el-button>
          <el-button v-if="row.status === 2" link type="success" @click="handleFinish(row)">完成</el-button>
          <el-button v-if="row.status !== 2 && row.status !== 4" link type="danger" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-pagination">
      <el-pagination background layout="total, sizes, prev, pager, next" :total="total"
                     v-model:current-page="query.current" v-model:page-size="query.size"
                     :page-sizes="[10, 20, 50]" @current-change="load" @size-change="reload" />
    </div>

    <el-drawer v-model="detailVisible" title="订单详情" size="520px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.receiver }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="商品金额">¥{{ detail.goodsAmount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="配送费">¥{{ detail.deliveryFee.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠">-¥{{ detail.discountAmount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付"><span class="money">¥{{ detail.payAmount.toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ detail.payType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="配送员">{{ detail.deliverymanId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ detail.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ detail.addressDetail }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 18px 0 8px">商品明细</h4>
        <el-table :data="detail.items" size="small" border>
          <el-table-column prop="productName" label="商品" min-width="140" />
          <el-table-column label="单价" width="90">
            <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="deliverVisible" title="订单发货" width="420px">
      <el-form label-width="100px">
        <el-form-item label="配送员 ID">
          <el-input-number v-model="deliverymanId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDeliver">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { cancelOrder, deliverOrder, finishOrder, getAdminOrders, getAdminOrderDetail, payOrder } from '@/api/order'
import { ORDER_STATUS_TAG, ORDER_STATUS_TEXT } from '@/types'
import type { OrderQuery, OrderVO, OrderItemVO } from '@/types'

const loading = ref(false)
const orders = ref<OrderVO[]>([])
const total = ref(0)
const query = reactive<OrderQuery>({ status: undefined, keyword: '', current: 1, size: 10 })
const detailVisible = ref(false)
const detail = ref<OrderVO | null>(null)
const deliverVisible = ref(false)
const deliverymanId = ref(1)
const deliveringOrderId = ref<number | null>(null)

async function load(): Promise<void> {
  loading.value = true
  try {
    const params: Record<string, unknown> = { current: query.current, size: query.size }
    if (query.status !== undefined) params.status = query.status
    if (query.keyword) params.keyword = query.keyword
    const page = await getAdminOrders(params)
    orders.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function reload(): void {
  query.current = 1
  void load()
}

async function openDetail(row: OrderVO): Promise<void> {
  detail.value = await getAdminOrderDetail(row.id)
  detailVisible.value = true
}

async function handlePay(row: OrderVO): Promise<void> {
  await ElMessageBox.confirm(`确认对订单 ${row.orderNo} 模拟收款？`, '提示', { type: 'warning' })
  await payOrder(row.id)
  ElMessage.success('收款成功')
  await load()
}

function openDeliver(row: OrderVO): void {
  deliveringOrderId.value = row.id
  deliverVisible.value = true
}

async function confirmDeliver(): Promise<void> {
  if (deliveringOrderId.value === null) return
  await deliverOrder(deliveringOrderId.value, deliverymanId.value)
  deliverVisible.value = false
  ElMessage.success('发货成功')
  await load()
}

async function handleFinish(row: OrderVO): Promise<void> {
  await ElMessageBox.confirm('确认订单已送达并完成？完成后将发放积分。', '提示', { type: 'info' })
  await finishOrder(row.id)
  ElMessage.success('订单已完成')
  await load()
}

async function handleCancel(row: OrderVO): Promise<void> {
  await ElMessageBox.confirm('取消订单将回滚库存、优惠券与积分，确认继续？', '警告', { type: 'warning' })
  await cancelOrder(row.id)
  ElMessage.success('订单已取消')
  await load()
}

onMounted(() => void load())
</script>
