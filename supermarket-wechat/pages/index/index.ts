import { getCategories, getProducts } from '../../api/product'
import { getStore } from '../../api/store'
import { addToCart } from '../../api/cart'
import { ensureLogin } from '../../utils/auth'
import type { Category, ProductVO, StoreSetting } from '../../typings/types'

Page({
  data: {
    store: null as StoreSetting | null,
    categories: [] as Category[],
    products: [] as ProductVO[],
    searchKeyword: '',
  },

  onLoad() {
    void this.loadData()
  },

  onPullDownRefresh() {
    void this.loadData().finally(() => wx.stopPullDownRefresh())
  },

  async loadData(): Promise<void> {
    const [store, categories, page] = await Promise.all([
      getStore(),
      getCategories(),
      getProducts({ isHot: 1, status: 1, current: 1, size: 20 }),
    ])
    this.setData({ store, categories, products: page.records })
  },

  onSearchInput(event: WechatMiniprogram.Input) {
    this.setData({ searchKeyword: event.detail.value })
  },

  goSearch() {
    const keyword = this.data.searchKeyword.trim()
    wx.navigateTo({ url: `/pages/list/list${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}` })
  },

  goCategory(event: WechatMiniprogram.BaseEvent) {
    const id = (event.currentTarget as any).dataset.id
    wx.navigateTo({ url: `/pages/list/list?categoryId=${id}` })
  },

  goAllCategory() {
    wx.reLaunch({ url: '/pages/category/category' })
  },

  async onAdd(event: WechatMiniprogram.CustomEvent) {
    if (!ensureLogin()) return
    const product = event.detail.product as ProductVO
    await addToCart(product.id, 1)
    wx.showToast({ title: '已加入购物车', icon: 'success' })
  },
})
