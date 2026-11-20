import { login } from '../../api/auth'
import { setToken } from '../../utils/auth'
import { DEMO_CODE } from '../../config'
import type { UserVO } from '../../typings/types'

Page({
  data: {
    phone: '',
    code: DEMO_CODE,
    submitting: false,
  },

  onPhoneInput(event: WechatMiniprogram.Input) {
    this.setData({ phone: event.detail.value })
  },

  onCodeInput(event: WechatMiniprogram.Input) {
    this.setData({ code: event.detail.value })
  },

  async onSubmit() {
    const phone = this.data.phone.trim()
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入 11 位手机号', icon: 'none' })
      return
    }
    if (!this.data.code.trim()) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const user: UserVO = await login(phone, this.data.code.trim())
      setToken(user.token || '')
      const app = getApp<IAppOption>()
      if (app) {
        app.globalData.token = user.token || ''
        app.globalData.userInfo = user as unknown as Record<string, unknown>
      }
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.reLaunch({ url: '/pages/index/index' })
        }
      }, 600)
    } finally {
      this.setData({ submitting: false })
    }
  },
})
