import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/Layout.vue'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '后台登录' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '数据概览' } },
      { path: 'orders', name: 'OrderManage', component: () => import('@/views/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'products', name: 'ProductManage', component: () => import('@/views/ProductManage.vue'), meta: { title: '商品管理' } },
      { path: 'categories', name: 'CategoryManage', component: () => import('@/views/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'reviews', name: 'ReviewManage', component: () => import('@/views/ReviewManage.vue'), meta: { title: '评价管理' } },
      { path: 'members', name: 'MemberManage', component: () => import('@/views/MemberManage.vue'), meta: { title: '会员管理' } },
      { path: 'coupons', name: 'CouponManage', component: () => import('@/views/CouponManage.vue'), meta: { title: '优惠券管理' } },
      { path: 'delivery', name: 'DeliveryManage', component: () => import('@/views/DeliveryManage.vue'), meta: { title: '配送管理' } },
      { path: 'store', name: 'StoreSetting', component: () => import('@/views/StoreSetting.vue'), meta: { title: '门店设置' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && getToken()) {
    return { path: '/dashboard' }
  }
  if (typeof to.meta.title === 'string') {
    document.title = `${to.meta.title} · 邻里小超管理后台`
  }
  return true
})

export default router
