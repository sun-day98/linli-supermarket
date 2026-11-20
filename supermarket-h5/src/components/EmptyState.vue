<template>
  <div class="empty-state">
    <div class="empty-state__box">□</div>
    <div class="empty-state__text">{{ text }}</div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{ text: string }>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-light);
}

.empty-state__box {
  font-size: 46px;
  line-height: 1;
  border: 2px solid var(--border);
  border-radius: 12px;
  padding: 10px 18px;
  margin-bottom: 12px;
}

.empty-state__text {
  font-size: 13px;
  margin-bottom: 12px;
}
</style>
