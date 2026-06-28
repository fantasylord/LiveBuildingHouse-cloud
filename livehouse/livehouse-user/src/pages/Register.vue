<template>
  <div class="register-page">
    <van-nav-bar title="用户注册" left-arrow @click-left="goBack" />
    
    <div class="register-form">
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
            v-model="form.code" 
            label="验证码" 
            placeholder="请输入验证码" 
            required
          >
            <template #button>
              <van-button size="small" type="primary" @click="sendCode">
                {{ codeBtnText }}
              </van-button>
            </template>
          </van-field>
          <van-field 
            v-model="form.username" 
            label="用户名" 
            placeholder="请输入用户名" 
            required 
          />
          <van-field 
            v-model="form.password" 
            label="密码" 
            placeholder="请输入密码" 
            type="password"
            required 
          />
          <van-field 
            v-model="form.confirmPassword" 
            label="确认密码" 
            placeholder="请再次输入密码" 
            type="password"
            required 
          />
        </van-cell-group>
        
        <div style="margin: 20px;">
          <van-button type="primary" block native-type="submit">注册</van-button>
        </div>
      </van-form>
      
      <div class="login-link">
        <span>已有账号？</span>
        <a href="/login">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi, sendCodeApi } from '@/api/user.js'

const router = useRouter()

const form = reactive({
  phone: '',
  code: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const codeBtnText = ref('获取验证码')
const codeCountdown = ref(0)

const goBack = () => {
  router.back()
}

const sendCode = async () => {
  if (!form.phone) {
    alert('请输入手机号')
    return
  }
  
  try {
    await sendCodeApi({ phone: form.phone })
    codeCountdown.value = 60
    codeBtnText.value = '60s后重发'
    
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
        codeBtnText.value = '获取验证码'
      } else {
        codeBtnText.value = `${codeCountdown.value}s后重发`
      }
    }, 1000)
  } catch (error) {
    alert('发送验证码失败')
  }
}

const onSubmit = async () => {
  if (!form.phone || !form.password) {
    alert('请填写手机号和密码')
    return
  }
  
  if (form.password !== form.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  try {
    const res = await registerApi({
      phone: form.phone,
      password: form.password,
      username: form.username,
      realName: form.username
    })
    
    if (res.data && res.data.data) {
      const data = res.data.data
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      alert('注册成功')
      setTimeout(() => {
        router.push('/')
      }, 1500)
    }
  } catch (error) {
    const message = error.response?.data?.message || '注册失败'
    alert(message)
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.register-form {
  padding: 20px;
}

.login-link {
  text-align: center;
  color: #999;
  margin-top: 20px;
}

.login-link a {
  color: #1989fa;
  margin-left: 5px;
}
</style>