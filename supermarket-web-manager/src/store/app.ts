import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 后台布局状态。 */
export const useAppStore = defineStore('adminApp', () => {
  const sidebarCollapsed = ref(false)
  const pageLoading = ref(false)

  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return { sidebarCollapsed, pageLoading, toggleSidebar }
})
