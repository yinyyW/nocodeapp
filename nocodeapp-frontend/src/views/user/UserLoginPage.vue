<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import { message } from "ant-design-vue";
import { UserOutlined, LockOutlined } from "@ant-design/icons-vue";
import { login } from "@/api/userController";
import { useLoginUserStore } from "@/stores/loginUser"
import { USER_ROLE } from "@/common/userRole"

const router = useRouter();
const route = useRoute();
const loginStore = useLoginUserStore();

const formRef = ref();
const loading = ref(false);

const loginForm = reactive<API.UserLoginRequest>({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const handleLogin = async () => {
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    const res = await login(loginForm);
    if (res?.data?.code === 0 && res?.data?.data) {
      message.success("登录成功");
      const userInfo = res.data.data;
      loginStore.setLoginUser({
        id: userInfo.id,
        userAccount: userInfo.userAccount,
        userName: userInfo.userName,
        userAvatar: userInfo.userAvatar,
        userProfile: userInfo.userProfile,
        userRole: userInfo.userRole as USER_ROLE,
        createTime: userInfo.createTime,
      });
      loginStore.setFetchUserFlag(false)
      const redirect = (route.query?.redirect as string) || "/";

      // 处理 redirect 为完整 URL 的情况，避免路由拼接错误
      if (redirect.startsWith("http://") || redirect.startsWith("https://")) {
        try {
          const url = new URL(redirect);
          router.push(url.pathname + url.search + url.hash);
        } catch {
          router.push("/");
        }
      } else {
        router.push(redirect);
      }
    } else {
      message.error(res?.data?.message ?? "登录失败");
    }
  } catch (e) {
    message.error((e as Error)?.message ?? "网络异常，请稍后重试");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录到 NoCodeApp</p>
        </div>

        <a-form ref="formRef" :model="loginForm" :rules="rules" autocomplete="off" @keyup.enter="handleLogin">
          <a-form-item name="username">
            <a-input v-model:value="loginForm.username" placeholder="请输入账号" size="large" allow-clear>
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password">
            <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" size="large" allow-clear>
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button type="primary" :loading="loading" block size="large" class="submit-btn" @click="handleLogin">
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          还没有账号？
          <router-link to="/user/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.submit-btn {
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.login-footer a {
  color: #1677ff;
}

@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 28px 20px;
  }

  .login-title {
    font-size: 24px;
  }
}
</style>
