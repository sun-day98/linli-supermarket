<template>
  <div>
    <PageHeader title="会员管理" description="按订单数据汇总的会员消费概览">
      <el-button @click="load">刷新</el-button>
    </PageHeader>

    <el-alert type="info" show-icon :closable="false" style="margin-bottom: 14px"
              title="演示环境后端未提供独立会员列表接口，本页由 /admin/orders 聚合生成会员消费概览。" />

    <el-table :data="members" v-loading="loading" size="small" stripe border>
      <el-table-column label="会员 ID" width="110" prop="userId" />
      <el-table-column label="收货人" width="120" prop="receiver" />
      <el-table-column label="手机号" width="140" prop="phone" />
      <el-table-column label="订单数" width="100" prop="orderCount" />
      <el-table-column label="累计消费" width="130">
        <template #default="{ row }"><span class="money">¥{{ row.totalAmount.toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column label="最近下单" min-width="180" prop="lastOrderAt" />
    </el-table>

    <div class="table-pagination">
      <el-pagination background layout="total, prev, pager, next" :total="members.length"
                     v-model:current-page="current" :page-size="10" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { getMembers } from '@/api/member'
import type { MemberSummary } from '@/types'

const loading = ref(false)
const allMembers = ref<MemberSummary[]>([])
const current = ref(1)
const members = computed(() => allMembers.value.slice((current.value - 1) * 10, current.value * 10))

async function load(): Promise<void> {
  loading.value = true
  try {
    allMembers.value = await getMembers()
  } finally {
    loading.value = false
  }
}

onMounted(() => void load())
</script>
