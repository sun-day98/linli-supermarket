import { getCategories, getProducts } from '../../api/product'
import { money } from '../../utils/util'
import type { Category, ProductVO } from '../../typings/types'

interface ProductView extends ProductVO {
  priceText: string
}

Page({
  data: {
    categories: [] as Category[],
    products: [] as ProductView[],
    currentId: 0,
    currentName: '',
  },

  onLoad() {
    void this.loadCategories()
  },

  async loadCategories(): Promise<void> {
    const categories = await getCategories()
    this.setData({ categories })
    if (categories.length > 0) {
      this.setData({ currentId: categories[0].id, currentName: categories[0].name })
      void this.loadProducts(categories[0].id)
    }
  },

  async loadProducts(categoryId: number): Promise<void> {
    const page = await getProducts({ categoryId, status: 1, current: 1, size: 50 })
    const products = page.records.map((item) => ({ ...item, priceText: money(item.price) }))
    this.setData({ products })
  },

  onCategoryTap(event: WechatMiniprogram.BaseEvent) {
    const dataset = (event.currentTarget as any).dataset
    const id = Number(dataset.id)
    if (id === this.data.currentId) return
    this.setData({ currentId: id, currentName: String(dataset.name) })
    void this.loadProducts(id)
  },

  onProductTap(event: WechatMiniprogram.BaseEvent) {
    const id = (event.currentTarget as any).dataset.id
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` })
  },
})
