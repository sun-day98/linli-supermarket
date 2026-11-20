import { cancelOrder, finishOrder, getOrder, payOrder } from '../../api/order'
import { ensureLogin } from '../../utils/auth'
import { dateTime, money, orderStatusText } from '../../utils/util'
import type { OrderItemVO, OrderVO } from '../../typings/types'

interface ItemView extends OrderItemVO {
  priceText: string
  totalText: string
}

interface OrderDetailView extends OrderVO {
  statusText: string
  goodsText: string
  deliveryText: string
  discountText: string
  payText: string
  createdText: string
  payTimeText: string
  finishTimeText: string
  items: ItemView[]
}

Page({
  data: {
    orderId: 0,
    autoPay: false,
    order: null as OrderDetailView | null,
  },

  onLoad(options: Record<string, string | undefined>) {
    if (!ensureLogin()) return
    const id = Number(options.id || 0)
    this.setData({ orderId: id, autoPay: options.pay === '1' })
    void this.load(id)
  },

  async load(id: number): Promise<void> {
    const order = await getOrder(id)
    const view: OrderDetailView = {
      ...order,
      statusText: order.statusText || orderStatusText(order.status),
      goodsText: money(order.goodsAmount),
      deliveryText: money(order.deliveryFee),
      discountText: money(order.discountAmount),
      payText: money(order.payAmount),
      createdText: dateTime(order.createdAt),
      payTimeText: dateTime(order.payTime),
      finishTimeText: dateTime(order.finishTime),
      items: order.items.map((item) => ({
        ...item,
        priceText: money(item.price),
        totalText: money(item.totalAmount),
      })),
    }
    this.setData({ order: view })
    if (this.data.autoPay && view.status === 0) {
      this.setData({ autoPay: false })
      this.onPay()
    }
  },

  onPay() {
    const order = this.data.order
    if (!order || order.status !== 0) return
    wx.showActionSheet({
      itemList: ['微信支付', '支付宝'],
      success: (res) => {
        const payType = res.tapIndex === 0 ? 'WECHAT' : 'ALIPAY'
        void payOrder(order.id, payType).then(() => this.load(order.id))
      },
    })
  },

  onCancel() {
    const order = this.data.order
    if (!order) return
    wx.showModal({
      title: '提示',
      content: '确认取消该订单？取消后库存与优惠券将释放。',
      success: (res) => {
        if (res.confirm) void cancelOrder(order.id).then(() => this.load(order.id))
      },
    })
  },

  onFinish() {
    const order = this.data.order
    if (!order) return
    wx.showModal({
      title: '提示',
      content: '确认已收到商品？确认后订单完成并发放积分。',
      success: (res) => {
        if (res.confirm) void finishOrder(order.id).then(() => this.load(order.id))
      },
    })
  },

  onCopyOrderNo() {
    const order = this.data.order
    if (!order) return
    wx.setClipboardData({ data: order.orderNo })
  },
})
