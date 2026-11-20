<template>
  <div>
    <PageHeader title="评价管理" description="商品评价审核与清理">
      <el-button @click="load">刷新</el-button>
    </PageHeader>

    <div class="table-toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable style="width: 150px" @change="reload">
        <el-option label="待审核" :value="0" />
        <el-option label="通过" :value="1" />
        <el-option label="拒绝" :value="2" />
      </el-select>
    </div>

    <el-table :data="reviews" v-loading="loading" size="small" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品 ID" width="100" prop="productId" />
      <el-table-column label="用户 ID" width="100" prop="userId" />
      <el-table-column label="评分" width="120">
        <template #default="{ row }"><span style="color: #ffb400">{{ '★'.repeat(row.rating) }}</span></template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="220" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'" size="small">
            {{ REVIEW_STATUS_TEXT[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="评价时间" min-width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="success" @click="audit(row, 1)">通过</el-button>
          <el-button v-if="row.status !== 2" link type="warning" @click="audit(row, 2)">拒绝</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-pagination">
      <el-pagination background layout="total, prev, pager, next" :total="total"
                     v-model:current-page="current" :page-size="20" @current-change="load" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { auditReview, deleteReview, getAdminReviews } from '@/api/review'
import { REVIEW_STATUS_TEXT } from '@/types'
import type { Review } from '@/types'

const loading = ref(false)
const reviews = ref<Review[]>([])
const total = ref(0)
const current = ref(1)
const status = ref<number | undefined>(undefined)

async function load(): Promise<void> {
  loading.value = true
  try {
    const page = await getAdminReviews(status.value, current.value, 20)
    reviews.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function reload(): void {
  current.value = 1
  void load()
}

async function audit(row: Review, nextStatus: number): Promise<void> {
  await auditReview(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '审核通过' : '已拒绝')
  await load()
}

async function remove(row: Review): Promise<void> {
  await ElMessageBox.confirm('确认删除该评价？', '警告', { type: 'warning' })
  await deleteReview(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => void load())
</script>
