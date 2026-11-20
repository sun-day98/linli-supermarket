/** 自定义底部导航：使用 reLaunch 切换 5 个主页面。 */
Component({
  properties: {
    current: { type: String, value: 'home' },
  },

  data: {
    tabs: [
      { key: 'home', label: '首页', url: '/pages/index/index' },
      { key: 'category', label: '分类', url: '/pages/category/category' },
      { key: 'cart', label: '购物车', url: '/pages/cart/cart' },
      { key: 'orders', label: '订单', url: '/pages/orders/orders' },
      { key: 'mine', label: '我的', url: '/pages/mine/mine' },
    ],
  },

  methods: {
    onTap(event: WechatMiniprogram.BaseEvent): void {
      const url = (event.currentTarget as any).dataset.url as string
      if (!url) return
      wx.reLaunch({ url })
    },
  },
})
