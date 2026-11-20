import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { adminLogin } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/auth'
import type { AdminProfile } from '@/types'

/** 管理员会话，Token key 与 H5 一致（linli_token）。 */
export const useUserStore = defineStore('adminUser', () => {
  const token = ref<string>(getToken())
  const profile = ref<AdminProfile | null>(null)
  const isLogin = computed(() => token.value.length > 0)
  const displayName = computed(() => profile.value?.realName || profile.value?.username || '管理员')

  async function login(username: string, password: string): Promise<void> {
    const data = await adminLogin({ username, password })
    token.value = data.token || ''
    setToken(token.value)
    profile.value = data
  }

  function logout(): void {
    token.value = ''
    profile.value = null
    removeToken()
  }

  return { token, profile, isLogin, displayName, login, logout }
})
