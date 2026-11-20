<template>
  <div class="page-header">
    <div>
      <h2>{{ title }}</h2>
      <p v-if="description">{{ description }}</p>
    </div>
    <div class="page-header__actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; description?: string }>()
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.page-header h2 {
  margin: 0;
  font-size: 19px;
}

.page-header p {
  margin: 4px 0 0;
  color: #909399;
  font-size: 12px;
}

.page-header__actions {
  display: flex;
  gap: 8px;
}
</style>
