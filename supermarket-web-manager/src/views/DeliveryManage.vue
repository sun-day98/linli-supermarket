<template>
  <div>
    <PageHeader title="配送管理" description="城市 / 区县可配送范围与配送员维护">
      <el-button type="primary" @click="openAreaForm()">新增区域</el-button>
      <el-button @click="loadAll">刷新</el-button>
    </PageHeader>

    <el-row :gutter="14">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="row" style="justify-content: space-between">
              <span>配送区域树</span>
              <span class="muted">城市停用或其下无启用区县则不展示</span>
            </div>
          </template>
          <el-tree :data="areas" :props="{ children: 'children', label: 'name' }" default-expand-all>
            <template #default="{ data }">
              <span class="row" style="flex: 1; justify-content: space-between">
                <span>
                  {{ data.name }}
                  <el-tag v-if="data.hot === 1" type="danger" size="small" style="margin-left: 6px">热门</el-tag>
                  <el-tag v-if="data.enabled === 0" type="info" size="small" style="margin-left: 6px">停用</el-tag>
                </span>
                <span>
                  <el-button link type="primary" @click.stop="openAreaForm(undefined, data)">新增区县</el-button>
                  <el-button link type="primary" @click.stop="openAreaForm(data)">编辑</el-button>
                  <el-button link type="danger" @click.stop="removeArea(data)">删除</el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="row" style="justify-content: space-between">
              <span>配送员</span>
              <el-button type="primary" size="small" @click="openDeliverymanForm()">新增</el-button>
            </div>
          </template>
          <el-table :data="deliverymen" size="small" border>
            <el-table-column prop="name" label="姓名" min-width="90" />
            <el-table-column prop="phone" label="手机号" min-width="120" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '在岗' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDeliverymanForm(row)">编辑</el-button>
                <el-button link type="danger" @click="removeDeliveryman(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="areaFormVisible" :title="editingAreaId ? '编辑区域' : '新增区域'" width="460px">
      <el-form :model="areaForm" label-width="90px">
        <el-form-item label="区域名称" required><el-input v-model="areaForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="areaForm.type" style="width: 100%">
            <el-option label="城市" :value="1" />
            <el-option label="区县" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="areaForm.type === 2" label="所属城市">
          <el-select v-model="areaForm.parentId" style="width: 100%">
            <el-option v-for="city in cities" :key="city.id" :label="city.name" :value="city.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="areaForm.sort" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="areaEnabled" /></el-form-item>
        <el-form-item label="热门城市"><el-switch v-model="areaHot" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="areaFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveArea">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deliverymanFormVisible" :title="editingDeliverymanId ? '编辑配送员' : '新增配送员'" width="440px">
      <el-form :model="deliverymanForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="deliverymanForm.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="deliverymanForm.phone" /></el-form-item>
        <el-form-item label="在岗"><el-switch v-model="deliverymanEnabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverymanFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDeliveryman">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import {
  createArea, createDeliveryman, deleteArea, deleteDeliveryman,
  getAreas, getDeliverymen, updateArea, updateDeliveryman,
} from '@/api/delivery'
import type { DeliveryArea, DeliveryAreaDTO, Deliveryman } from '@/types'

const areas = ref<DeliveryArea[]>([])
const deliverymen = ref<Deliveryman[]>([])
const areaFormVisible = ref(false)
const editingAreaId = ref<number | null>(null)
const areaEnabled = ref(true)
const areaHot = ref(false)
const areaForm = reactive<DeliveryAreaDTO>({ parentId: 0, name: '', type: 1, enabled: 1, hot: 0, sort: 0 })

const deliverymanFormVisible = ref(false)
const editingDeliverymanId = ref<number | null>(null)
const deliverymanEnabled = ref(true)
const deliverymanForm = reactive<Deliveryman>({ name: '', phone: '', status: 1 })

/** 顶层城市由后端树结构提供，供区县选择所属城市。 */
const cities = computed(() => areas.value.map((item) => ({
  id: item.id, parentId: 0, name: item.name, type: 1, enabled: item.enabled, hot: item.hot, sort: item.sort,
})))

async function loadAll(): Promise<void> {
  const [areaList, men] = await Promise.all([getAreas(), getDeliverymen()])
  areas.value = areaList
  deliverymen.value = men
}

function openAreaForm(area?: DeliveryArea, parent?: DeliveryArea): void {
  editingAreaId.value = area?.id ?? null
  areaForm.parentId = parent?.id ?? area?.parentId ?? 0
  areaForm.name = area?.name ?? ''
  areaForm.type = parent ? 2 : area?.type ?? 1
  areaForm.sort = area?.sort ?? 0
  areaEnabled.value = area ? area.enabled === 1 : true
  areaHot.value = area ? area.hot === 1 : false
  areaFormVisible.value = true
}

function areaPayload(): DeliveryAreaDTO {
  return {
    ...areaForm,
    parentId: areaForm.type === 2 ? areaForm.parentId : 0,
    enabled: areaEnabled.value ? 1 : 0,
    hot: areaHot.value ? 1 : 0,
  }
}

async function saveArea(): Promise<void> {
  if (!areaForm.name) {
    ElMessage.warning('请输入区域名称')
    return
  }
  if (editingAreaId.value) await updateArea(editingAreaId.value, areaPayload())
  else await createArea(areaPayload())
  areaFormVisible.value = false
  ElMessage.success('保存成功')
  await loadAll()
}

async function removeArea(area: DeliveryArea): Promise<void> {
  await ElMessageBox.confirm(`删除「${area.name}」将同时删除其下区县，确认继续？`, '警告', { type: 'warning' })
  await deleteArea(area.id)
  ElMessage.success('删除成功')
  await loadAll()
}

function openDeliverymanForm(man?: Deliveryman): void {
  editingDeliverymanId.value = man?.id ?? null
  deliverymanForm.name = man?.name ?? ''
  deliverymanForm.phone = man?.phone ?? ''
  deliverymanEnabled.value = man ? man.status === 1 : true
  deliverymanFormVisible.value = true
}

async function saveDeliveryman(): Promise<void> {
  if (!deliverymanForm.name || !deliverymanForm.phone) {
    ElMessage.warning('请填写姓名和手机号')
    return
  }
  const payload: Deliveryman = { ...deliverymanForm, status: deliverymanEnabled.value ? 1 : 0 }
  if (editingDeliverymanId.value) await updateDeliveryman(editingDeliverymanId.value, payload)
  else await createDeliveryman(payload)
  deliverymanFormVisible.value = false
  ElMessage.success('保存成功')
  await loadAll()
}

async function removeDeliveryman(man: Deliveryman): Promise<void> {
  await ElMessageBox.confirm(`确认删除配送员「${man.name}」？`, '警告', { type: 'warning' })
  if (man.id) await deleteDeliveryman(man.id)
  ElMessage.success('删除成功')
  await loadAll()
}

onMounted(() => void loadAll())
</script>
