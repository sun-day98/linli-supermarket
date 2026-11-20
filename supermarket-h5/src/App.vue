<template>
  <div class="app-shell">
    <router-view v-slot="{ Component }">
      <component :is="Component" />
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'

const userStore = useUserStore()
const cartStore = useCartStore()

onMounted(() => {
  if (userStore.isLogin) {
    void userStore.fetchProfile()
    void cartStore.sync()
  }
})
</script>

<style scoped>
.app-shell {
  width: 100%;
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-main);
}
</style>
