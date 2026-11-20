<template>
  <div class="login">
    <div class="login__hero">
      <div class="login__logo">邻</div>
      <div class="login__title">邻里小超</div>
      <div class="login__slogan">新鲜到家 · 邻里共享</div>
    </div>

    <div class="card login__form">
      <div class="field">
        <label>手机号</label>
        <input v-model="phone" type="tel" maxlength="11" placeholder="请输入手机号" />
      </div>
      <div class="field">
        <label>验证码</label>
        <input v-model="code" type="text" maxlength="6" placeholder="演示环境固定 123456" />
      </div>
      <button class="btn login__submit" :disabled="loading" @click="submit">
        {{ loading ? '登录中...' : '登录 / 注册' }}
      </button>
      <div class="login__tip">未注册手机号首次登录将自动创建账号并发放新人券</div>
      <button class="login__demo" @click="fillDemo">使用演示账号 13912348765</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { toast } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const phone = ref('')
const code = ref('123456')
const loading = ref(false)

function fillDemo(): void {
  phone.value = '13912348765'
  code.value = '123456'
}

async function submit(): Promise<void> {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    toast('请输入正确的手机号')
    return
  }
  if (!code.value) {
    toast('请输入验证码')
    return
  }
  loading.value = true
  try {
    await userStore.login(phone.value, code.value)
    await cartStore.sync()
    toast('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    void router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--brand) 0%, #4cd964 32%, var(--bg-page) 32%);
}

.login__hero {
  padding: 60px 0 36px;
  text-align: center;
  color: #fff;
}

.login__logo {
  width: 66px;
  height: 66px;
  margin: 0 auto 10px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 700;
}

.login__title {
  font-size: 22px;
  font-weight: 700;
}

.login__slogan {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

.login__form {
  margin-top: 0;
  padding: 22px 18px;
}

.login__submit {
  width: 100%;
  margin-top: 20px;
  padding: 11px;
  font-size: 15px;
}

.login__tip {
  color: var(--text-sub);
  font-size: 12px;
  text-align: center;
  margin-top: 12px;
}

.login__demo {
  display: block;
  margin: 16px auto 0;
  border: none;
  background: transparent;
  color: var(--brand);
  font-size: 13px;
}
</style>
