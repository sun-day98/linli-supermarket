import { getProducts } from '../../api/product'
import { addToCart } from '../../api/cart'
import { ensureLogin } from '../../utils/auth'
import type { ProductVO } from '../../typings/types'

Page({
  data: {
    products: [] as ProductVO[],
    keyword: '',
    categoryId: 0,
    current: 1,
    total: 0,
    hotOnly: false,
  },

  onLoad(options: Record<string, string>) {
    const keyword = options.keyword ? decodeURIComponent(options.keyword) : ''
    this.setData({
      keyword,
      categoryId: options.categoryId ? Number(options.categoryId) : 0,
    })
    void this.loadProducts(true)
  },

  onReachBottom() {
    if (this.data.products.length < this.data.total) void this.loadProducts(false)
  },

  async loadProducts(reset: boolean): Promise<void> {
    const params: Record<string, unknown> = {
      status: 1,
      current: reset ? 1 : this.data.current + 1,
      size: 12,
    }
    if (this.data.categoryId) params.categoryId = this.data.categoryId
    if (this.data.keyword) params.keyword = this.data.keyword
    if (this.data.hotOnly) params.isHot = 1
    const page = await getProducts(params)
    this.setData({
      products: reset ? page.records : this.data.products.concat(page.records),
      total: page.total,
      current: reset ? 1 : this.data.current + 1,
    })
  },

  onInput(event: WechatMiniprogram.Input) {
    this.setData({ keyword: event.detail.value })
  },

  onSearch() {
    this.setData({ keyword: this.data.keyword.trim() })
    void this.loadProducts(true)
  },

  toggleHot() {
    this.setData({ hotOnly: !this.data.hotOnly })
    void this.loadProducts(true)
  },

  async onAdd(event: WechatMiniprogram.CustomEvent) {
    if (!ensureLogin()) return
    const product = event.detail.product as ProductVO
    await addToCart(product.id, 1)
    wx.showToast({ title: '已加入购物车', icon: 'success' })
  },
})
