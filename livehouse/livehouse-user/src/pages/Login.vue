<template>
  <div class="login-page">
    <div class="logo-section">
      <van-icon name="home-o" size="80" color="#1989fa" />
      <div class="logo-text">LiveHouse</div>
      <div class="logo-subtitle">高端楼盘智慧看房</div>
    </div>
    
    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field 
            v-model="form.phone" 
            label="手机号" 
            placeholder="请输入手机号" 
            type="number"
            required 
          />
          <van-field 
            v-model="form.password" 
            label="密码" 
            placeholder="请输入密码" 
            type="password"
            required 
          />
        </van-cell-group>
        
        <div class="form-options">
          <van-checkbox v-model="rememberMe">记住我</van-checkbox>
          <a href="#" class="forgot-link">忘记密码</a>
        </div>
        
        <div style="margin: 20px;">
          <van-button type="primary" block native-type="submit">登录</van-button>
        </div>
      </van-form>
      
      <div class="register-link">
        <span>还没有账号？</span>
        <a href="/register">立即注册</a>
      </div>
      
      <div class="other-login">
        <div class="divider">
          <span>其他登录方式</span>
        </div>
        <div class="social-buttons">
          <van-button icon="wechat" type="default" size="small">微信登录</van-button>
          <van-button icon="phone" type="default" size="small">短信登录</van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi } from '@/api/user.js'

const router = useRouter()

const form = reactive({
  phone: '',
  password: ''
})

const rememberMe = ref(false)

const onSubmit = async () => {
  if (!form.phone || !form.password) {
    alert('请填写手机号和密码')
    return
  }
  
  try {
    const res = await loginApi({
      phone: form.phone,
      password: form.password
    })
    
    if (res.data && res.data.data) {
      const data = res.data.data
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      alert('登录成功')
      setTimeout(() => {
        router.push('/')
      }, 1500)
    }
  } catch (error) {
    const message = error.response?.data?.message || '登录失败'
    alert(message)
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40px 20px;
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #1989fa;
  margin-top: 15px;
}

.logo-subtitle {
  color: #999;
  margin-top: 5px;
}

.login-form {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.forgot-link {
  color: #1989fa;
}

.register-link {
  text-align: center;
  color: #999;
  margin-top: 20px;
}

.register-link a {
  color: #1989fa;
  margin-left: 5px;
}

.other-login {
  margin-top: 30px;
}

.divider {
  text-align: center;
  color: #999;
  margin-bottom: 15px;
}

.social-buttons {
  display: flex;
  gap: 10px;
}

.social-buttons .van-button {
  flex: 1;
}
</style>