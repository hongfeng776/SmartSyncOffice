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
        
        <el-form-item v-if="showCaptcha" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              prefix-icon="Picture"
              :size="isMobile ? 'default' : 'large'"
              class="captcha-input"
              maxlength="4"
            />
            <div class="captcha-image" @click="refreshCaptcha" title="点击刷新">
              <img v-if="captchaInfo.captchaImage" :src="captchaInfo.captchaImage" alt="验证码" />
              <span v-else class="refresh-loading">加载中...</span>
            </div>
          </div>
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
            :disabled="isLocked"
            class="login-btn"
            @click="handleLogin"
          >
            <span v-if="!loading">
              <template v-if="isLocked">
                账号已锁定 ({{ lockCountdown }}s后解锁)
              </template>
              <template v-else>登 录</template>
            </span>
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
    
    <el-dialog
      v-model="showDifferentLocationDialog"
      title="异地登录提醒"
      width="480px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @close="cancelLogin"
    >
      <div class="different-location-content">
        <el-alert
          title="检测到异地登录"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        >
          为确保账户安全，我们检测到您当前登录地点与上次不同
        </el-alert>
        
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="上次登录IP">
            {{ differentLocationInfo.lastLoginIp }}
          </el-descriptions-item>
          <el-descriptions-item label="上次登录时间">
            {{ differentLocationInfo.lastLoginTime }}
          </el-descriptions-item>
          <el-descriptions-item label="当前登录IP">
            {{ differentLocationInfo.currentLoginIp }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="security-tips" style="margin-top: 16px">
          <el-icon><Warning /></el-icon>
          <span>如果不是您本人操作，请立即修改密码并检查账户安全</span>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="cancelLogin">取消登录</el-button>
        <el-button type="primary" @click="confirmLogin">
          <el-icon><Lock /></el-icon>
          确认登录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import axios from 'axios'
import { login as apiLogin, getUserInfo } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const isMobile = ref(false)
const showCaptcha = ref(false)
const showDifferentLocationDialog = ref(false)
const differentLocationInfo = ref({
  lastLoginIp: '',
  lastLoginTime: '',
  currentLoginIp: ''
})
const differentLocationData = ref(null)

const isLocked = ref(false)
const lockCountdown = ref(0)
let lockTimer = null
let resizeTimer = null

const captchaInfo = reactive({
  captchaKey: '',
  captchaImage: ''
})

const loginForm = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: '',
  rememberMe: false,
  autoLogin: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

function handleResize() {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(checkMobile, 100)
}

async function refreshCaptcha() {
  try {
    const res = await axios.get('/api/captcha/generate')
    if (res.data && res.data.code === 200) {
      captchaInfo.captchaKey = res.data.data.captchaKey
      captchaInfo.captchaImage = res.data.data.captchaImage
      loginForm.captchaKey = res.data.data.captchaKey
      loginForm.captchaCode = ''
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请重试')
  }
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

function startLockCountdown(minutes) {
  isLocked.value = true
  lockCountdown.value = minutes * 60
  lockTimer = setInterval(() => {
    lockCountdown.value--
    if (lockCountdown.value <= 0) {
      clearInterval(lockTimer)
      isLocked.value = false
    }
  }, 1000)
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const loginParams = {
      username: loginForm.username,
      password: loginForm.password
    }
    
    if (showCaptcha.value) {
      loginParams.captchaCode = loginForm.captchaCode
      loginParams.captchaKey = loginForm.captchaKey
    }
    
    const res = await apiLogin(loginParams)
    
    if (res.data.differentLocation) {
      differentLocationInfo.value = {
        lastLoginIp: res.data.lastLoginIp || '未知',
        lastLoginTime: res.data.lastLoginTime || '首次登录',
        currentLoginIp: res.data.currentLoginIp || '未知'
      }
      differentLocationData.value = res.data
      showDifferentLocationDialog.value = true
      loading.value = false
      return
    }
    
    await completeLogin(res)
  } catch (error) {
    console.error('登录失败:', error)
    const errorCode = error?.response?.data?.code
    const errorMsg = error?.response?.data?.message || '登录失败'
    
    if (errorCode === 1006 || errorCode === 1007) {
      showCaptcha.value = true
      refreshCaptcha()
    } else if (errorCode === 1008) {
      const match = errorMsg.match(/(\d+)分钟后重试/)
      if (match) {
        startLockCountdown(parseInt(match[1]))
      }
    } else if (errorCode === 1002) {
      if (!showCaptcha.value) {
        showCaptcha.value = true
        refreshCaptcha()
      }
    }
  } finally {
    if (!showDifferentLocationDialog.value) {
      loading.value = false
    }
  }
}

async function completeLogin(res) {
  userStore.token = res.data.token
  userStore.menus = []
  userStore.userInfo = null
  
  try {
    await userStore.getUserInfo()
    ElMessage.success('登录成功')
    handleRememberedInfo()
    handleAutoLogin()
    
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    userStore.resetState()
    ElMessage.error('获取用户信息失败')
  }
}

async function confirmLogin() {
  if (differentLocationData.value) {
    showDifferentLocationDialog.value = false
    loading.value = true
    
    try {
      userStore.token = differentLocationData.value.token
      userStore.menus = []
      userStore.userInfo = null
      
      await userStore.getUserInfo()
      
      ElMessage.success('登录成功')
      handleRememberedInfo()
      handleAutoLogin()
      
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      userStore.resetState()
      ElMessage.error('获取用户信息失败')
    } finally {
      loading.value = false
    }
  }
}

function cancelLogin() {
  showDifferentLocationDialog.value = false
  userStore.resetState()
  loading.value = false
}

onMounted(() => {
  checkMobile()
  loadRememberedInfo()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (resizeTimer) clearTimeout(resizeTimer)
  if (lockTimer) clearInterval(lockTimer)
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  height: auto;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: auto;
  padding: 20px 0;
}

.login-bg {
  position: fixed;
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
  box-sizing: border-box;
}

@media screen and (max-width: 768px) {
  .login-box {
    width: 90%;
    max-width: 360px;
    padding: 30px 20px;
  }
}

@media screen and (max-width: 480px) {
  .login-box {
    width: 95%;
    padding: 20px 16px;
  }
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

  .subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
    font-weight: 400;
  }
}

@media screen and (max-width: 768px) {
  .login-header {
    margin-bottom: 20px;
  }
  
  .login-header .title {
    font-size: 22px;
  }
  
  .login-header .subtitle {
    font-size: 12px;
  }
}

.login-form {
  margin-bottom: 20px;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  
  .captcha-input {
    flex: 1;
    min-width: 0;
  }
  
  .captcha-image {
    width: 120px;
    height: 40px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    transition: all 0.3s;
    flex-shrink: 0;
    
    &:hover {
      border-color: #409eff;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
    }
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .refresh-loading {
      font-size: 12px;
      color: #909399;
    }
  }
}

@media screen and (max-width: 480px) {
  .captcha-row .captcha-image {
    width: 100px;
    height: 36px;
  }
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

@media screen and (max-width: 480px) {
  .login-options {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
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

  &:hover:not(:disabled) {
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

.different-location-content {
  .security-tips {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #fef0f0;
    border-radius: 4px;
    color: #f56c6c;
    font-size: 13px;
    
    .el-icon {
      font-size: 18px;
    }
  }
}

@media screen and (max-height: 650px) {
  .login-header {
    margin-bottom: 16px;
  }

  .login-header .title {
    font-size: 22px;
  }

  .login-header .subtitle {
    display: none;
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
</style>
