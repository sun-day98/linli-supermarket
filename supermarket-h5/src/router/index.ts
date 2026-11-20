import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
  { path: '/category', name: 'Category', component: () => import('@/views/Category.vue'), meta: { title: '分类' } },
  { path: '/products', name: 'ProductList', component: () => import('@/views/ProductList.vue'), meta: { title: '商品列表' } },
  { path: '/products/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },
  { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { title: '购物车', requiresAuth: true } },
  { path: '/checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { title: '确认订单', requiresAuth: true } },
  { path: '/orders', name: 'OrderList', component: () => import('@/views/OrderList.vue'), meta: { title: '我的订单', requiresAuth: true } },
  { path: '/orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { title: '订单详情', requiresAuth: true } },
  { path: '/member', name: 'Member', component: () => import('@/views/Member.vue'), meta: { title: '会员中心', requiresAuth: true } },
  { path: '/points', name: 'Points', component: () => import('@/views/Points.vue'), meta: { title: '我的积分', requiresAuth: true } },
  { path: '/coupons', name: 'Coupons', component: () => import('@/views/Coupons.vue'), meta: { title: '我的优惠券', requiresAuth: true } },
  { path: '/address', name: 'Address', component: () => import('@/views/Address.vue'), meta: { title: '收货地址', requiresAuth: true } },
  { path: '/service', name: 'Service', component: () => import('@/views/Service.vue'), meta: { title: '客服中心' } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (typeof to.meta.title === 'string') {
    document.title = `${to.meta.title} · 邻里小超`
  }
  return true
})

export default router
