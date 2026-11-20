<template>
  <nav class="tab-bar">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="tab-bar__item"
      active-class="tab-bar__item--active"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
           stroke-linecap="round" stroke-linejoin="round" class="tab-bar__icon">
        <path :d="tab.icon" />
      </svg>
      <span>{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
const tabs = [
  { path: '/', label: '首页', icon: 'M3 10.5 12 3l9 7.5V21h-6v-6H9v6H3z' },
  { path: '/category', label: '分类', icon: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z' },
  { path: '/cart', label: '购物车', icon: 'M3 4h2l2.4 11h10L20 7H6M9 20a1 1 0 1 0 0-2 1 1 0 0 0 0 2M18 20a1 1 0 1 0 0-2 1 1 0 0 0 0 2' },
  { path: '/orders', label: '订单', icon: 'M6 2h9l5 5v15H6zM15 2v5h5M9 12h8M9 16h8' },
  { path: '/member', label: '我的', icon: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM5 21a7 7 0 0 1 14 0' },
]
</script>

<style scoped>
.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: var(--tabbar-height);
  display: flex;
  background: #fff;
  border-top: 1px solid var(--border);
  z-index: 50;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-bar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--text-sub);
  font-size: 11px;
}

.tab-bar__item--active {
  color: var(--brand);
  font-weight: 600;
}

.tab-bar__icon {
  width: 22px;
  height: 22px;
}
</style>
