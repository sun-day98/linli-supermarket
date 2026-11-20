import { getAddresses, getMyCoupons, getPointsRecords, getProfile } from '../../api/member'
import { getStore } from '../../api/store'
import { fetchMe } from '../../api/auth'
import { getToken, isLogin, removeToken } from '../../utils/auth'
import { couponStatusText, dateTime, money } from '../../utils/util'
import type { Address, CouponVO, PointsRecord, StoreSetting, UserVO } from '../../typings/types'

interface PointsView extends PointsRecord {
  createdText: string
  typeText: string
  pointsText: string
}

interface CouponView extends CouponVO {
  discountText: string
  statusText: string
}

const POINT_TYPE_TEXT = ['', '获取', '使用', '回滚']

Page({
  data: {
    logged: false,
    profile: null as UserVO | null,
    pointsRecords: [] as PointsView[],
    coupons: [] as CouponView[],
    addresses: [] as Address[],
    store: null as StoreSetting | null,
    servicePhone: '400-800-1234',
  },

  onShow() {
    const logged = isLogin()
    this.setData({ logged })
    if (!logged) return
    void this.load()
  },

  async load(): Promise<void> {
    const [profile, pointsPage, coupons, addresses, store] = await Promise.all([
      getProfile(),
      getPointsRecords(),
      getMyCoupons(),
      getAddresses(),
      getStore(),
    ])
    const pointsRecords: PointsView[] = pointsPage.records.map((record) => ({
      ...record,
      createdText: dateTime(record.createdAt),
      typeText: POINT_TYPE_TEXT[record.type] || '积分',
      pointsText: `${record.points > 0 ? '+' : ''}${record.points}`,
    }))
    const couponViews: CouponView[] = coupons.map((coupon) => ({
      ...coupon,
      discountText: coupon.type === 2 ? `${coupon.discount}折` : money(coupon.discount),
      statusText: couponStatusText(coupon.userCouponStatus),
    }))
    this.setData({
      profile,
      pointsRecords,
      coupons: couponViews,
      addresses,
      store,
      servicePhone: store.servicePhone || '400-800-1234',
    })
  },

  onLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  async onRefreshProfile() {
    if (!getToken()) return
    try {
      const profile = await fetchMe()
      this.setData({ profile })
      wx.showToast({ title: '资料已更新', icon: 'success' })
    } catch {
      // 请求层已统一提示错误
    }
  },

  onOrdersTap(event: WechatMiniprogram.BaseEvent) {
    const status = (event.currentTarget as any).dataset.status
    const url = status === undefined
      ? '/pages/orders/orders'
      : `/pages/orders/orders?status=${status}`
    wx.navigateTo({ url })
  },

  onCoupons() {
    const content = this.data.coupons.length
      ? this.data.coupons
          .slice(0, 8)
          .map((c) => `${c.name} ${c.discountText}（满${c.threshold}元）${c.statusText}`)
          .join('\n')
      : '暂无优惠券，下单时可使用新人券'
    wx.showModal({ title: '我的优惠券', content, showCancel: false })
  },

  onAddresses() {
    const content = this.data.addresses.length
      ? this.data.addresses
          .map((a) => `${a.receiver} ${a.phone}\n${a.province}${a.city}${a.district}${a.detail}`)
          .join('\n\n')
      : '暂无收货地址，下单时可选择'
    wx.showModal({ title: '收货地址', content, showCancel: false })
  },

  onPointsRecords() {
    wx.showModal({
      title: '积分明细',
      content: this.data.pointsRecords.length
        ? this.data.pointsRecords
            .slice(0, 8)
            .map((r) => `${r.createdText} ${r.typeText} ${r.pointsText}`)
            .join('\n')
        : '暂无积分记录',
      showCancel: false,
    })
  },

  onService() {
    wx.showModal({
      title: '联系客服',
      content: `客服热线：${this.data.servicePhone}\n服务时间：09:00 - 21:00`,
      confirmText: '拨打电话',
      success: (res) => {
        if (res.confirm) {
          wx.makePhoneCall({ phoneNumber: this.data.servicePhone })
        }
      },
    })
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确认退出登录？',
      success: (res) => {
        if (!res.confirm) return
        removeToken()
        this.setData({
          logged: false,
          profile: null,
          pointsRecords: [],
          coupons: [],
          addresses: [],
        })
      },
    })
  },
})
