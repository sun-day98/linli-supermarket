import { getCart, removeCartItem, updateCartItem } from '../../api/cart'
import { ensureLogin } from '../../utils/auth'
import { money } from '../../utils/util'
import type { CartVO } from '../../typings/types'

interface CartView extends CartVO {
  priceText: string
  subtotalText: string
}

Page({
  data: {
    items: [] as CartView[],
    totalCount: 0,
    totalAmountText: '¥0.00',
    allSelected: false,
  },

  onShow() {
    if (!ensureLogin()) return
    void this.load()
  },

  async load(): Promise<void> {
    const cart = await getCart()
    const items = cart.map((item) => ({
      ...item,
      priceText: money(item.price),
      subtotalText: money(item.subtotal),
    }))
    const selected = items.filter((item) => item.selected === 1)
    this.setData({
      items,
      totalCount: selected.reduce((sum, item) => sum + item.quantity, 0),
      totalAmountText: money(selected.reduce((sum, item) => sum + item.subtotal, 0)),
      allSelected: items.length > 0 && selected.length === items.length,
    })
  },

  async onToggle(event: WechatMiniprogram.BaseEvent) {
    const dataset = (event.currentTarget as any).dataset
    const selected = Number(dataset.selected) === 1 ? 0 : 1
    await updateCartItem(Number(dataset.id), Number(dataset.productId), Number(dataset.quantity), selected)
    await this.load()
  },

  async onChangeQty(event: WechatMiniprogram.BaseEvent) {
    const dataset = (event.currentTarget as any).dataset
    const quantity = Number(dataset.quantity) + Number(dataset.delta)
    if (quantity < 1 || quantity > Number(dataset.stock)) return
    await updateCartItem(Number(dataset.id), Number(dataset.productId), quantity, Number(dataset.selected))
    await this.load()
  },

  onRemove(event: WechatMiniprogram.BaseEvent) {
    const id = Number((event.currentTarget as any).dataset.id)
    wx.showModal({
      title: '提示',
      content: '确认从购物车移除该商品？',
      success: (res) => {
        if (res.confirm) void removeCartItem(id).then(() => this.load())
      },
    })
  },

  async onToggleAll() {
    const target = this.data.allSelected ? 0 : 1
    for (const item of this.data.items) {
      if (item.selected !== target) {
        await updateCartItem(item.id, item.productId, item.quantity, target)
      }
    }
    await this.load()
  },

  onCheckout() {
    if (this.data.totalCount === 0) {
      wx.showToast({ title: '请先勾选商品', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/checkout/checkout' })
  },
})
