import { getToken } from './utils/auth'

/** 小程序入口：启动时从本地存储恢复 Token。 */
App<IAppOption>({
  globalData: {
    token: '',
    userInfo: null,
  },

  onLaunch() {
    const token = getToken()
    this.globalData.token = token
    if (token) {
      // 资料由会员中心页面按需拉取，避免启动阶段阻塞首页展示
      this.globalData.userInfo = null
    }
  },
})
