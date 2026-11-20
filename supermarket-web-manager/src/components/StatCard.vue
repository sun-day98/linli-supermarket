<template>
  <el-card shadow="hover" class="stat-card">
    <div class="stat-card__head">
      <span>{{ title }}</span>
      <el-icon :style="{ color: color }"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-card__value" :style="{ color }">{{ value }}</div>
    <div class="stat-card__foot">{{ tip }}</div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  value: string | number
  tip?: string
  icon: string
  color?: string
}>()
</script>

<style scoped>
.stat-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #909399;
  font-size: 13px;
}

.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  margin: 10px 0 4px;
}

.stat-card__foot {
  color: #a8abb2;
  font-size: 12px;
}
</style>
