<template>
  <div>
    <PageHeader title="门店设置" description="门店名称、宣传语、配送费与客服电话">
      <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
    </PageHeader>

    <el-card v-loading="loading" style="max-width: 760px">
      <el-form :model="form" label-width="130px">
        <el-form-item label="门店名称"><el-input v-model="form.storeName" /></el-form-item>
        <el-form-item label="宣传语"><el-input v-model="form.slogan" /></el-form-item>
        <el-form-item label="门店公告">
          <el-input v-model="form.notice" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="配送费">
          <el-input-number v-model="form.deliveryFee" :min="0" :precision="2" />
          <span class="muted" style="margin-left: 10px">元</span>
        </el-form-item>
        <el-form-item label="免配送门槛">
          <el-input-number v-model="form.freeDeliveryThreshold" :min="0" :precision="2" />
          <span class="muted" style="margin-left: 10px">商品金额达到此值免配送费</span>
        </el-form-item>
        <el-form-item label="客服电话"><el-input v-model="form.servicePhone" /></el-form-item>
        <el-form-item label="Logo URL"><el-input v-model="form.logo" /></el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" title="保存后 H5 与小程序首页、结算页会实时读取最新配置。" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getStore, updateStore } from '@/api/store'
import type { StoreSetting } from '@/types'

const loading = ref(false)
const saving = ref(false)
const form = reactive<StoreSetting>({
  id: 1, storeName: '', slogan: '', notice: '', deliveryFee: 5,
  freeDeliveryThreshold: 59, servicePhone: '', logo: '',
})

async function load(): Promise<void> {
  loading.value = true
  try {
    Object.assign(form, await getStore())
  } finally {
    loading.value = false
  }
}

async function save(): Promise<void> {
  saving.value = true
  try {
    await updateStore({ ...form })
    ElMessage.success('门店设置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => void load())
</script>
