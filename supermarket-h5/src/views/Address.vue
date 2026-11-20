<template>
  <div class="page plain">
    <header class="simple-header">
      <button class="back" @click="router.back()">‹</button>收货地址
    </header>

    <div v-for="item in addresses" :key="item.id" class="card address-item">
      <div class="row-between">
        <strong>{{ item.receiver }}</strong>
        <span class="muted">{{ item.phone }}</span>
      </div>
      <div class="muted address-item__detail">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}</div>
      <div class="row-between address-item__actions">
        <div>
          <span v-if="item.isDefault === 1" class="address-item__default">默认地址</span>
          <button v-else class="link" @click="setDefault(item.id)">设为默认</button>
        </div>
        <div>
          <button class="link" @click="edit(item)">编辑</button>
          <button class="link link--danger" @click="remove(item.id)">删除</button>
        </div>
      </div>
    </div>
    <EmptyState v-if="addresses.length === 0" text="暂无收货地址" />

    <button class="btn address-add" @click="openForm()">+ 新增收货地址</button>

    <div v-if="formVisible" class="mask" @click.self="formVisible = false">
      <div class="sheet">
        <div class="sheet__title">{{ editingId ? '编辑地址' : '新增地址' }}</div>
        <div class="field"><label>收货人</label><input v-model="form.receiver" placeholder="请输入姓名" /></div>
        <div class="field"><label>手机号</label><input v-model="form.phone" placeholder="11 位手机号" /></div>
        <div class="field"><label>省 / 市</label><input v-model="form.province" placeholder="如 北京市" /></div>
        <div class="field"><label>城市</label><input v-model="form.city" placeholder="如 北京市" /></div>
        <div class="field"><label>区县</label><input v-model="form.district" placeholder="需在可配送范围内" /></div>
        <div class="field"><label>详细地址</label><input v-model="form.detail" placeholder="小区 / 楼栋 / 门牌" /></div>
        <div class="field">
          <label>默认地址</label>
          <input v-model="form.isDefault" type="checkbox" style="flex: none; width: 18px; height: 18px" />
        </div>
        <button class="btn address-add" @click="save">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import { createAddress, getAddresses, removeAddress, setDefaultAddress, updateAddress } from '@/api/address'
import { toast } from '@/utils/format'
import type { Address, AddressDTO } from '@/types'

const router = useRouter()
const addresses = ref<Address[]>([])
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<AddressDTO>({
  receiver: '',
  phone: '',
  province: '北京市',
  city: '北京市',
  district: '朝阳区',
  detail: '',
  isDefault: 0,
})

async function load(): Promise<void> {
  addresses.value = await getAddresses()
}

function openForm(item?: Address): void {
  editingId.value = item?.id ?? null
  form.receiver = item?.receiver ?? ''
  form.phone = item?.phone ?? ''
  form.province = item?.province ?? '北京市'
  form.city = item?.city ?? '北京市'
  form.district = item?.district ?? '朝阳区'
  form.detail = item?.detail ?? ''
  form.isDefault = item?.isDefault ?? 0
  formVisible.value = true
}

function edit(item: Address): void {
  openForm(item)
}

async function save(): Promise<void> {
  if (!form.receiver || !form.phone || !form.detail) {
    toast('请填写完整收货信息')
    return
  }
  if (editingId.value) {
    await updateAddress(editingId.value, { ...form })
  } else {
    await createAddress({ ...form })
  }
  formVisible.value = false
  await load()
}

async function setDefault(id: number): Promise<void> {
  await setDefaultAddress(id)
  await load()
}

async function remove(id: number): Promise<void> {
  await removeAddress(id)
  await load()
}

onMounted(() => void load())
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

.address-item__detail {
  margin-top: 6px;
  font-size: 13px;
}

.address-item__actions {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--border);
  font-size: 12px;
}

.address-item__default {
  color: var(--brand);
}

.link {
  border: none;
  background: transparent;
  color: var(--brand);
  font-size: 12px;
  margin-left: 10px;
}

.link--danger {
  color: var(--danger);
}

.address-add {
  display: block;
  width: calc(100% - 24px);
  margin: 16px 12px;
}

.address-add:not(.btn) {
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  border-radius: 999px;
}
</style>
