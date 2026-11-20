<template>
  <div>
    <PageHeader title="优惠券管理" description="券类型、门槛、有效期、发放范围与新人自动发放">
      <el-button type="primary" @click="openForm()">新增优惠券</el-button>
      <el-button @click="load">刷新</el-button>
    </PageHeader>

    <el-table :data="coupons" v-loading="loading" size="small" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="券名称" min-width="180" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ row.typeName }}</template>
      </el-table-column>
      <el-table-column label="优惠" width="110">
        <template #default="{ row }">
          {{ row.type === 2 ? `${(row.discount * 10).toFixed(1)}折` : `¥${row.discount.toFixed(2)}` }}
        </template>
      </el-table-column>
      <el-table-column label="门槛" width="100">
        <template #default="{ row }">满¥{{ row.threshold.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="发放" width="120">
        <template #default="{ row }">{{ row.issued }} / {{ row.total }}</template>
      </el-table-column>
      <el-table-column label="新人自动" width="100">
        <template #default="{ row }">
          <el-tag :type="row.newUserAuto === 1 ? 'success' : 'info'" size="small">
            {{ row.newUserAuto === 1 ? '开启' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-pagination">
      <el-pagination background layout="total, prev, pager, next" :total="total"
                     v-model:current-page="current" :page-size="20" @current-change="load" />
    </div>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑优惠券' : '新增优惠券'" width="620px">
      <el-form :model="form" label-width="100px">
        <div class="form-grid">
          <el-form-item label="券名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="券类型" required>
            <el-select v-model="form.type" style="width: 100%">
              <el-option label="满减券" :value="1" />
              <el-option label="折扣券（抵扣比例）" :value="2" />
              <el-option label="无门槛券" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item :label="form.type === 2 ? '抵扣比例' : '优惠金额'">
            <el-input-number v-model="form.discount" :min="0" :precision="2" :max="form.type === 2 ? 1 : 99999"
                             style="width: 100%" />
          </el-form-item>
          <el-form-item label="使用门槛"><el-input-number v-model="form.threshold" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="有效期">
            <el-select v-model="form.validType" style="width: 100%">
              <el-option label="固定起止日期" :value="1" />
              <el-option label="领取后 N 天" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.validType === 2" label="有效天数">
            <el-input-number v-model="form.validDays" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="发放总量"><el-input-number v-model="form.total" :min="1" style="width: 100%" /></el-form-item>
          <el-form-item label="发放范围">
            <el-select v-model="form.scope" style="width: 100%">
              <el-option label="全部用户" :value="1" />
              <el-option label="新人专享" :value="2" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item v-if="form.validType === 1" label="起止时间">
          <el-date-picker v-model="dateRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
                          range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 100%" />
        </el-form-item>
        <el-form-item label="新人自动发放">
          <el-switch v-model="newUserAutoBool" />
          <span class="muted" style="margin-left: 10px">开启后注册即自动发券</span>
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="enabledBool" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { createCoupon, deleteCoupon, getAdminCoupons, updateCoupon } from '@/api/coupon'
import type { CouponDTO, CouponVO } from '@/types'

const loading = ref(false)
const coupons = ref<CouponVO[]>([])
const total = ref(0)
const current = ref(1)
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const dateRange = ref<string[]>([])
const newUserAutoBool = ref(false)
const enabledBool = ref(true)
const form = reactive({
  name: '', type: 1, discount: 5, threshold: 30, validType: 2,
  validDays: 30, scope: 1, total: 100, newUserAuto: 0, status: 1,
  startTime: null as string | null, endTime: null as string | null,
})

async function load(): Promise<void> {
  loading.value = true
  try {
    const page = await getAdminCoupons(current.value, 20)
    coupons.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function openForm(coupon?: CouponVO): void {
  editingId.value = coupon?.id ?? null
  Object.assign(form, {
    name: coupon?.name ?? '',
    type: coupon?.type ?? 1,
    discount: coupon?.discount ?? 5,
    threshold: coupon?.threshold ?? 30,
    validType: coupon?.validType ?? 2,
    validDays: coupon?.validDays ?? 30,
    scope: coupon?.scope ?? 1,
    total: coupon?.total ?? 100,
    startTime: coupon?.startTime ?? null,
    endTime: coupon?.endTime ?? null,
    newUserAuto: coupon?.newUserAuto ?? 0,
    status: coupon?.status ?? 1,
  })
  dateRange.value = coupon?.startTime && coupon?.endTime ? [coupon.startTime, coupon.endTime] : []
  newUserAutoBool.value = form.newUserAuto === 1
  enabledBool.value = form.status === 1
  formVisible.value = true
}

function toDTO(): CouponDTO {
  return {
    ...form,
    startTime: form.validType === 1 ? (dateRange.value[0] ?? null) : null,
    endTime: form.validType === 1 ? (dateRange.value[1] ?? null) : null,
    newUserAuto: newUserAutoBool.value ? 1 : 0,
    status: enabledBool.value ? 1 : 0,
  }
}

async function save(): Promise<void> {
  if (!form.name) {
    ElMessage.warning('请输入券名称')
    return
  }
  const dto = toDTO()
  if (editingId.value) await updateCoupon(editingId.value, dto)
  else await createCoupon(dto)
  formVisible.value = false
  ElMessage.success('保存成功')
  await load()
}

async function remove(row: CouponVO): Promise<void> {
  await ElMessageBox.confirm(`确认删除优惠券「${row.name}」？`, '警告', { type: 'warning' })
  await deleteCoupon(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => void load())
</script>
