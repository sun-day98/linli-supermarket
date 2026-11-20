import { getAddresses } from '../../api/member'
import { getAvailableCoupons } from '../../api/member'
import { getCart } from '../../api/cart'
import { createOrder, previewOrder } from '../../api/order'
import { ensureLogin } from '../../utils/auth'
import { money } from '../../utils/util'
import type { Address, CartVO, CheckoutPreviewVO, CouponVO } from '../../typings/types'

interface CartView extends CartVO {
  subtotalText: string
}

/** 券在 WXML 中不能调用 TS helper，需预计算展示文案。 */
interface CouponView extends CouponVO {
  discountText: string
}

Page({
  data: {
    items: [] as CartView[],
    addresses: [] as Address[],
    addressIndex: 0,
    coupons: [] as CouponView[],
    couponIndex: -1,
    preview: null as CheckoutPreviewVO | null,
    goodsText: '¥0.00',
    deliveryText: '¥0.00',
    discountText: '¥0.00',
    payText: '¥0.00',
    remark: '',
    addressSheet: false,
    couponSheet: false,
    submitting: false,
  },

  onLoad() {
    if (!ensureLogin()) return
    void this.load()
  },

  async load(): Promise<void> {
    const cart = await getCart()
    const items = cart.filter((item) => item.selected === 1).map((item) => ({
      ...item,
      subtotalText: money(item.subtotal),
    }))
    if (items.length === 0) {
      wx.showToast({ title: '请先勾选商品', icon: 'none' })
      return
    }
    const addresses = await getAddresses()
    const defaultIndex = Math.max(0, addresses.findIndex((item) => item.isDefault === 1))
    const goodsAmount = items.reduce((sum, item) => sum + item.subtotal, 0)
    const coupons: CouponView[] = (await getAvailableCoupons(goodsAmount)).map((coupon) => ({
      ...coupon,
      discountText: coupon.type === 2 ? `${coupon.discount}折` : money(coupon.discount),
    }))
    this.setData({ items, addresses, addressIndex: defaultIndex, coupons })
    await this.refreshPreview()
  },

  currentAddress(): Address | null {
    return this.data.addresses[this.data.addressIndex] || null
  },

  currentCouponId(): number | null {
    if (this.data.couponIndex < 0) return null
    return this.data.coupons[this.data.couponIndex].userCouponId
  },

  buildDTO(): Record<string, unknown> {
    return {
      items: this.data.items.map((item) => ({ productId: item.productId, quantity: item.quantity })),
      addressId: this.currentAddress() ? this.currentAddress().id : 0,
      couponId: this.currentCouponId(),
      remark: this.data.remark,
    }
  },

  async refreshPreview(): Promise<void> {
    if (!this.currentAddress()) return
    try {
      const preview = await previewOrder(this.buildDTO())
      this.setData({
        preview,
        goodsText: money(preview.goodsAmount),
        deliveryText: money(preview.deliveryFee),
        discountText: money(preview.discountAmount),
        payText: money(preview.payAmount),
      })
    } catch {
      this.setData({ couponIndex: -1 })
    }
  },

  openAddressSheet() {
    this.setData({ addressSheet: true })
  },

  closeAddressSheet() {
    this.setData({ addressSheet: false })
  },

  async chooseAddress(event: WechatMiniprogram.BaseEvent) {
    const index = Number((event.currentTarget as any).dataset.index)
    this.setData({ addressIndex: index, addressSheet: false })
    await this.refreshPreview()
  },

  openCouponSheet() {
    this.setData({ couponSheet: true })
  },

  closeCouponSheet() {
    this.setData({ couponSheet: false })
  },

  async chooseCoupon(event: WechatMiniprogram.BaseEvent) {
    const dataset = (event.currentTarget as any).dataset
    const index = Number(dataset.index)
    this.setData({ couponIndex: index, couponSheet: false })
    await this.refreshPreview()
  },

  onRemarkInput(event: WechatMiniprogram.Input) {
    this.setData({ remark: event.detail.value })
  },

  async onSubmit() {
    if (!this.currentAddress()) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }
    if (this.data.preview && !this.data.preview.deliverable) {
      wx.showToast({ title: '该地址暂不支持配送', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const dto = this.buildDTO()
      if (this.data.preview) dto.expectedPayAmount = this.data.preview.payAmount
      const order = await createOrder(dto)
      wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${order.id}&pay=1` })
    } finally {
      this.setData({ submitting: false })
    }
  },
})
