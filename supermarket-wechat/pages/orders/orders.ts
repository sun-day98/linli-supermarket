import { cancelOrder, finishOrder, getOrders, payOrder } from '../../api/order'
import { ensureLogin } from '../../utils/auth'
import { dateTime, money, orderStatusText } from '../../utils/util'
import type { OrderVO } from '../../typings/types'

interface OrderView extends OrderVO {
  statusText: string
  payText: string
  createdText: string
  itemSummary: string
  firstImage: string
}

interface Tab {
  label: string
  status: number | null
}

const TABS: Tab[] = [
  { label: '全部', status: null },
  { label: '待付款', status: 0 },
  { label: '待配送', status: 1 },
  { label: '配送中', status: 2 },
  { label: '已完成', status: 3 },
  { label: '已取消', status: 4 },
]

Page({
  data: {
    tabs: TABS,
    activeTab: 0,
    orders: [] as OrderView[],
  },

  onLoad(options: Record<string, string | undefined>) {
    const status = options.status
    if (status !== undefined) {
      const index = TABS.findIndex((tab) => tab.status === Number(status))
      if (index >= 0) this.setData({ activeTab: index })
    }
  },

  onShow() {
    if (!ensureLogin()) return
    void this.load()
  },

  async load(): Promise<void> {
    const status = TABS[this.data.activeTab].status
    const page = await getOrders(status === null ? undefined : status)
    const orders: OrderView[] = page.records.map((order) => ({
      ...order,
      statusText: order.statusText || orderStatusText(order.status),
      payText: money(order.payAmount),
      createdText: dateTime(order.createdAt),
      itemSummary: order.items.map((item) => item.productName).join('、'),
      firstImage: order.items.length > 0 ? order.items[0].productImage : '',
    }))
    this.setData({ orders })
  },

  onTabChange(event: WechatMiniprogram.BaseEvent) {
    const index = Number((event.currentTarget as any).dataset.index)
    if (index === this.data.activeTab) return
    this.setData({ activeTab: index })
    void this.load()
  },

  onOrderTap(event: WechatMiniprogram.BaseEvent) {
    const id = Number((event.currentTarget as any).dataset.id)
    wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${id}` })
  },

  onPay(event: WechatMiniprogram.BaseEvent) {
    const id = Number((event.currentTarget as any).dataset.id)
    wx.showActionSheet({
      itemList: ['微信支付', '支付宝'],
      success: (res) => {
        const payType = res.tapIndex === 0 ? 'WECHAT' : 'ALIPAY'
        void payOrder(id, payType).then(() => this.load())
      },
    })
  },

  onCancel(event: WechatMiniprogram.BaseEvent) {
    const id = Number((event.currentTarget as any).dataset.id)
    wx.showModal({
      title: '提示',
      content: '确认取消该订单？取消后库存与优惠券将释放。',
      success: (res) => {
        if (res.confirm) void cancelOrder(id).then(() => this.load())
      },
    })
  },

  onFinish(event: WechatMiniprogram.BaseEvent) {
    const id = Number((event.currentTarget as any).dataset.id)
    wx.showModal({
      title: '提示',
      content: '确认已收到商品？确认后订单完成并发放积分。',
      success: (res) => {
        if (res.confirm) void finishOrder(id).then(() => this.load())
      },
    })
  },
})
