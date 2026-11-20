import { getProduct, getReviews } from '../../api/product'
import { addToCart } from '../../api/cart'
import { ensureLogin } from '../../utils/auth'
import { dateTime, money } from '../../utils/util'
import type { ProductVO, Review } from '../../typings/types'

interface ReviewView extends Review {
  timeText: string
  stars: string
}

Page({
  data: {
    product: null as ProductVO | null,
    priceText: '¥0.00',
    originText: '¥0.00',
    reviews: [] as ReviewView[],
  },

  onLoad(options: Record<string, string>) {
    void this.load(Number(options.id))
  },

  async load(id: number): Promise<void> {
    const product = await getProduct(id)
    const page = await getReviews(id)
    const reviews = page.records.filter((item) => item.status === 1).map((item) => ({
      ...item,
      timeText: dateTime(item.createdAt),
      stars: '★'.repeat(item.rating),
    }))
    this.setData({
      product,
      priceText: money(product.price),
      originText: money(product.originalPrice),
      reviews,
    })
  },

  async add(): Promise<boolean> {
    if (!ensureLogin() || !this.data.product) return false
    await addToCart(this.data.product.id, 1)
    wx.showToast({ title: '已加入购物车', icon: 'success' })
    return true
  },

  async onAdd() {
    await this.add()
  },

  async onBuyNow() {
    const ok = await this.add()
    if (ok) wx.navigateTo({ url: '/pages/checkout/checkout' })
  },
})
