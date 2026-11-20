import { money } from '../../utils/util'
import type { ProductVO } from '../../typings/types'

/** 商品卡片：展示主图、名称、价格、库存，点击进详情，右侧按钮加购。 */
Component({
  properties: {
    product: {
      type: Object,
      value: {},
      observer(value: WechatMiniprogram.IAnyObject) {
        const product = value as unknown as ProductVO | null
        this.setData({
          priceText: money(product ? product.price : 0),
          originText: money(product ? product.originalPrice : 0),
          stockText: product ? `${product.stock}${product.unit || ''}可售` : '',
        })
      },
    },
  },

  data: {
    priceText: '¥0.00',
    originText: '¥0.00',
    stockText: '',
  },

  methods: {
    onTap(): void {
      const product = (this as any).data.product as ProductVO
      if (!product || !product.id) return
      wx.navigateTo({ url: `/pages/detail/detail?id=${product.id}` })
    },

    onAdd(): void {
      const product = (this as any).data.product as ProductVO
      if (!product || !product.id) return
      this.triggerEvent('add', { product })
    },
  },
})
