<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-card__logo">邻</div>
      <h2>邻里小超 · 管理后台</h2>
      <p class="muted">门店经营一体化管理平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="管理员账号" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
                    prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
      <div class="login-card__tip">演示账号：admin / admin123</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    void router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #07c160 0%, #004d3a 100%);
}

.login-card {
  width: 380px;
  background: #fff;
  border-radius: 14px;
  padding: 36px 34px 28px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  text-align: center;
}

.login-card__logo {
  width: 58px;
  height: 58px;
  margin: 0 auto 12px;
  border-radius: 16px;
  background: var(--el-color-primary, #07c160);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
}

.login-card h2 {
  margin: 0;
  font-size: 20px;
}

.login-card__tip {
  margin-top: 16px;
  color: #909399;
  font-size: 12px;
}
</style>
