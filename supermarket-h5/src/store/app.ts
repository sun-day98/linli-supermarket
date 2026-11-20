import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getStore } from '@/api/store'
import { toast } from '@/utils/format'
import type { StoreSetting } from '@/types'

/** 全局 UI 状态与门店信息缓存。 */
export const useAppStore = defineStore('app', () => {
  const loading = ref(0)
  const store = ref<StoreSetting | null>(null)

  function showLoading(): void {
    loading.value += 1
  }

  function hideLoading(): void {
    loading.value = Math.max(0, loading.value - 1)
  }

  async function loadStore(): Promise<StoreSetting | null> {
    try {
      store.value = await getStore()
      return store.value
    } catch {
      toast('门店信息加载失败')
      return null
    }
  }

  return { loading, store, showLoading, hideLoading, loadStore }
})
