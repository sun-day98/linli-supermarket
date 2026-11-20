<template>
  <el-container class="admin-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '210px'" class="admin-layout__aside">
      <div class="admin-layout__logo">
        <span v-if="!appStore.sidebarCollapsed">邻里小超 · 后台</span>
        <span v-else>邻</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#07c160"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-layout__header">
        <div class="row">
          <el-button text @click="appStore.toggleSidebar">
            <el-icon><component :is="appStore.sidebarCollapsed ? 'Expand' : 'Fold'" /></el-icon>
          </el-button>
          <span class="admin-layout__crumb">{{ route.meta.title || '管理后台' }}</span>
        </div>
        <div class="row">
          <span class="admin-layout__user">{{ userStore.displayName }}</span>
          <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-layout__main">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const menus = [
  { path: '/dashboard', title: '数据概览', icon: 'DataLine' },
  { path: '/orders', title: '订单管理', icon: 'List' },
  { path: '/products', title: '商品管理', icon: 'Goods' },
  { path: '/categories', title: '分类管理', icon: 'Files' },
  { path: '/reviews', title: '评价管理', icon: 'ChatDotRound' },
  { path: '/members', title: '会员管理', icon: 'User' },
  { path: '/coupons', title: '优惠券管理', icon: 'Ticket' },
  { path: '/delivery', title: '配送管理', icon: 'Truck' },
  { path: '/store', title: '门店设置', icon: 'Setting' },
]

async function logout(): Promise<void> {
  try {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    userStore.logout()
    void router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-layout__aside {
  background: #001529;
  transition: width 0.2s;
}

.admin-layout__logo {
  height: 58px;
  line-height: 58px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  background: #002140;
  white-space: nowrap;
  overflow: hidden;
}

.admin-layout__aside :deep(.el-menu) {
  border-right: none;
}

.admin-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8e9ec;
  height: 58px;
}

.admin-layout__crumb {
  font-weight: 600;
  margin-left: 8px;
}

.admin-layout__user {
  margin-right: 12px;
  color: #606266;
}

.admin-layout__main {
  background: #f0f2f5;
  padding: 16px;
}
</style>
