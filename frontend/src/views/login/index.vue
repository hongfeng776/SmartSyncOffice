<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
    
    <div class="login-box" :class="{ 'login-box-mobile': isMobile }">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="isMobile ? 40 : 48" color="#409EFF">
            <OfficeBuilding />
          </el-icon>
        </div>
        <h1 class="title" :class="{ 'title-mobile': isMobile }">SmartSyncOffice</h1>
        <p class="subtitle" :class="{ 'subtitle-mobile': isMobile }">分布式智能办公协同系统</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            :size="isMobile ? 'default' : 'large'"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            :size="isMobile ? 'default' : 'large'"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">
              <span class="checkbox-text">记住密码</span>
            </el-checkbox>
            <el-checkbox v-model="loginForm.autoLogin">
              <span class="checkbox-text">自动登录</span>
            </el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :size="isMobile ? 'default' : 'large'"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips" v-show="!isMobile">
        <el-alert
          title="测试账号"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>管理员：admin / 123456</p>
            <p>员工：employee / 123456</p>
          </template>
        </el-alert>
      </div>
      
      <div class="login-footer">
        <span class="copyright">© 2024 SmartSyncOffice 智能办公系统</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const isMobile = ref(false)
let resizeTimer = null

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false,
  autoLogin: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

function handleResize() {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(checkMobile, 100)
}

function loadRememberedInfo() {
  const remembered = localStorage.getItem('remembered_login')
  if (remembered) {
    const info = JSON.parse(remembered)
    loginForm.username = info.username || ''
    loginForm.password = info.password || ''
    loginForm.rememberMe = true
  }
}

function handleRememberedInfo() {
  if (loginForm.rememberMe) {
    localStorage.setItem('remembered_login', JSON.stringify({
      username: loginForm.username,
      password: loginForm.password
    }))
  } else {
    localStorage.removeItem('remembered_login')
  }
}

function handleAutoLogin() {
  if (loginForm.autoLogin) {
    localStorage.setItem('auto_login', 'true')
  } else {
    localStorage.removeItem('auto_login')
  }
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    handleRememberedInfo()
    handleAutoLogin()
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkMobile()
  loadRememberedInfo()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (resizeTimer) clearTimeout(resizeTimer)
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;

  .bg-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    background: #fff;
    animation: float 20s infinite ease-in-out;
  }

  .bg-circle-1 {
    width: 300px;
    height: 300px;
    top: 10%;
    left: 5%;
    animation-delay: 0s;
  }

  .bg-circle-2 {
    width: 400px;
    height: 400px;
    bottom: 10%;
    right: 5%;
    animation-delay: 5s;
  }

  .bg-circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    left: 50%;
    animation-delay: 10s;
  }

  @keyframes float {
    0%, 100% {
      transform: translate(0, 0) scale(1);
    }
    25% {
      transform: translate(30px, -30px) scale(1.1);
    }
    50% {
      transform: translate(-20px, 20px) scale(0.9);
    }
    75% {
      transform: translate(-30px, -20px) scale(1.05);
    }
  }
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.login-box-mobile {
  width: 90%;
  max-width: 360px;
  padding: 30px 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  .logo {
    margin-bottom: 12px;
  }

  .title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin: 0 0 8px 0;
    letter-spacing: 1px;
  }

  .title-mobile {
    font-size: 22px;
  }

  .subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
    font-weight: 400;
  }

  .subtitle-mobile {
    font-size: 12px;
  }
}

.login-form {
  margin-bottom: 20px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  width: 100%;

  .checkbox-text {
    font-size: 13px;
    color: #606266;
  }
}

.login-btn {
  width: 100%;
  font-size: 16px;
  height: 44px;
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 2px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  }
}

.login-tips {
  :deep(.el-alert) {
    border: none;
    background-color: #f4f4f5;
    border-radius: 8px;
  }

  p {
    margin: 4px 0;
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  .copyright {
    font-size: 12px;
    color: #909399;
  }
}

@media screen and (max-height: 600px) {
  .login-header {
    margin-bottom: 16px;

    .title {
      font-size: 22px;
    }

    .subtitle {
      display: none;
    }
  }

  .login-box {
    padding: 24px;
  }

  .login-tips {
    display: none;
  }

  .login-footer {
    display: none;
  }
}

@media screen and (max-width: 480px) {
  .login-box {
    width: 95%;
    padding: 20px 16px;
  }

  .login-header {
    margin-bottom: 20px;
  }

  .login-options {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
