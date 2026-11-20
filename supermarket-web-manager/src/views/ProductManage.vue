<template>
  <div>
    <PageHeader title="商品管理" description="商品价格、库存、上下架与库存紧张筛选">
      <el-button type="primary" @click="openForm()">新增商品</el-button>
      <el-button @click="load">刷新</el-button>
    </PageHeader>

    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="商品名称" clearable style="width: 200px" @keyup.enter="reload" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="reload">
        <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="上架状态" clearable style="width: 130px" @change="reload">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-checkbox v-model="lowStockOnly" @change="reload">仅看库存紧张</el-checkbox>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="products" v-loading="loading" size="small" stripe border>
      <el-table-column label="图片" width="70">
        <template #default="{ row }"><el-image :src="row.image" style="width: 46px; height: 46px" fit="cover" /></template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="categoryName" label="分类" width="110" />
      <el-table-column label="售价" width="100">
        <template #default="{ row }"><span class="money">¥{{ row.price.toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column label="库存" width="110">
        <template #default="{ row }">
          <el-tag :type="row.lowStock ? 'danger' : 'success'" size="small">
            {{ row.stock }}{{ row.unit }}<span v-if="row.lowStock"> 紧张</span>
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="上架" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="(v: boolean) => toggleStatus(row, v)" />
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
      <el-pagination background layout="total, sizes, prev, pager, next" :total="total"
                     v-model:current-page="query.current" v-model:page-size="query.size"
                     :page-sizes="[10, 20, 50]" @current-change="load" @size-change="reload" />
    </div>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑商品' : '新增商品'" width="720px">
      <el-form :model="form" label-width="86px">
        <div class="form-grid">
          <el-form-item label="商品名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="分类" required>
            <el-select v-model="form.categoryId" style="width: 100%">
              <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
          <el-form-item label="商品编码"><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="售价" required><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="划线价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="库存" required><el-input-number v-model="form.stock" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="紧张阈值"><el-input-number v-model="form.stockAlert" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="单位"><el-input v-model="form.unit" placeholder="斤 / 瓶 / 袋" /></el-form-item>
          <el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item>
          <el-form-item label="图片 URL"><el-input v-model="form.image" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="热门"><el-switch v-model="form.isHotBool" /></el-form-item>
          <el-form-item label="上架"><el-switch v-model="form.statusBool" /></el-form-item>
        </div>
        <el-form-item label="商品详情"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="营养信息"><el-input v-model="form.nutrition" type="textarea" :rows="2" /></el-form-item>
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
import { getCategories } from '@/api/category'
import { changeProductStatus, createProduct, deleteProduct, getProducts, updateProduct } from '@/api/product'
import type { Category, ProductDTO, ProductQuery, ProductVO } from '@/types'

interface ProductForm extends Omit<ProductDTO, 'isHot' | 'status'> {
  isHotBool: boolean
  statusBool: boolean
}

const loading = ref(false)
const products = ref<ProductVO[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const lowStockOnly = ref(false)
const query = reactive<ProductQuery>({ keyword: '', categoryId: undefined, status: undefined, current: 1, size: 10 })
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const emptyForm = (): ProductForm => ({
  categoryId: 0, name: '', subtitle: '', code: '', price: 0, originalPrice: 0,
  stock: 0, stockAlert: 10, sales: 0, unit: '件', spec: '', image: '', images: '[]',
  description: '', nutrition: '', sort: 0, isHotBool: false, statusBool: true,
})
const form = reactive<ProductForm>(emptyForm())

async function load(): Promise<void> {
  loading.value = true
  try {
    const params: Record<string, unknown> = { current: query.current, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId !== undefined) params.categoryId = query.categoryId
    if (query.status !== undefined) params.status = query.status
    if (lowStockOnly.value) params.lowStock = 1
    const page = await getProducts(params)
    products.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function reload(): void {
  query.current = 1
  void load()
}

function openForm(product?: ProductVO): void {
  editingId.value = product?.id ?? null
  Object.assign(form, emptyForm())
  if (product) {
    Object.assign(form, {
      categoryId: product.categoryId,
      name: product.name,
      subtitle: product.subtitle,
      code: product.code,
      price: product.price,
      originalPrice: product.originalPrice,
      stock: product.stock,
      stockAlert: product.stockAlert,
      sales: product.sales,
      unit: product.unit,
      spec: product.spec,
      image: product.image,
      images: product.images,
      description: product.description,
      nutrition: product.nutrition,
      sort: product.sort,
      isHotBool: product.isHot === 1,
      statusBool: product.status === 1,
    })
  } else if (categories.value.length > 0) {
    form.categoryId = categories.value[0].id
  }
  formVisible.value = true
}

function toDTO(): ProductDTO {
  return { ...form, isHot: form.isHotBool ? 1 : 0, status: form.statusBool ? 1 : 0 }
}

async function save(): Promise<void> {
  if (!form.name || !form.categoryId) {
    ElMessage.warning('请填写商品名称并选择分类')
    return
  }
  const dto = toDTO()
  if (editingId.value) await updateProduct(editingId.value, dto)
  else await createProduct(dto)
  formVisible.value = false
  ElMessage.success('保存成功')
  await load()
}

async function toggleStatus(row: ProductVO, value: boolean): Promise<void> {
  await changeProductStatus(row.id, value ? 1 : 0)
  ElMessage.success(value ? '已上架' : '已下架')
  await load()
}

async function remove(row: ProductVO): Promise<void> {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '警告', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  categories.value = await getCategories()
  await load()
})
</script>
