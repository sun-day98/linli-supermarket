import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchMe, login as loginApi } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'
import type { UserVO } from '@/types'

/** 用户会话：Token 持久化在 localStorage，key = linli_token。 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const profile = ref<UserVO | null>(null)
  const isLogin = computed(() => token.value.length > 0)

  async function login(phone: string, code = '123456'): Promise<void> {
    const data = await loginApi({ phone, code })
    token.value = data.token || ''
    setToken(token.value)
    profile.value = data
  }

  async function fetchProfile(): Promise<void> {
    if (!token.value) return
    profile.value = await fetchMe()
  }

  function logout(): void {
    token.value = ''
    profile.value = null
    removeToken()
  }

  return { token, profile, isLogin, login, logout, fetchProfile }
})
