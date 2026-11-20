<template>
  <div>
    <PageHeader title="分类管理" description="分类排序将同步到 H5 与小程序">
      <el-button type="primary" @click="openForm()">新增分类</el-button>
    </PageHeader>

    <el-table :data="categories" v-loading="loading" size="small" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="分类名称" min-width="160">
        <template #default="{ row }"><span class="row"><el-image :src="row.icon" style="width: 28px; height: 28px; margin-right: 8px" />{{ row.name }}</span></template>
      </el-table-column>
      <el-table-column label="排序" width="160">
        <template #default="{ row }">
          <el-input-number v-model="row.sort" :min="0" size="small" @change="(v: number) => saveSort(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图标 URL"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="enabled" /></el-form-item>
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
import { createCategory, deleteCategory, getCategories, updateCategory } from '@/api/category'
import type { Category } from '@/types'

const loading = ref(false)
const categories = ref<Category[]>([])
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const enabled = ref(true)
const form = reactive({ name: '', icon: '', sort: 0 })

async function load(): Promise<void> {
  loading.value = true
  try {
    categories.value = await getCategories()
  } finally {
    loading.value = false
  }
}

function openForm(category?: Category): void {
  editingId.value = category?.id ?? null
  form.name = category?.name ?? ''
  form.icon = category?.icon ?? ''
  form.sort = category?.sort ?? 0
  enabled.value = category ? category.status === 1 : true
  formVisible.value = true
}

function payload(): Omit<Category, 'id'> {
  return { name: form.name, icon: form.icon, sort: form.sort, status: enabled.value ? 1 : 0 }
}

async function save(): Promise<void> {
  if (!form.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (editingId.value) await updateCategory(editingId.value, payload())
  else await createCategory(payload())
  formVisible.value = false
  ElMessage.success('保存成功')
  await load()
}

async function saveSort(row: Category, sort: number): Promise<void> {
  await updateCategory(row.id, { name: row.name, icon: row.icon, sort, status: row.status })
  ElMessage.success('排序已同步')
}

async function remove(row: Category): Promise<void> {
  await ElMessageBox.confirm(`删除分类「${row.name}」会同时删除其下商品，确认继续？`, '警告', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => void load())
</script>
