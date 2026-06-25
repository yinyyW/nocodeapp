<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { UserOutlined, LockOutlined } from "@ant-design/icons-vue";
import { register } from "@/api/userController";
import type { RuleObject } from "ant-design-vue/es/form";

const router = useRouter();

const formRef = ref();
const loading = ref(false);

const registerForm = reactive<API.UserRegisterRequest>({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
});

const confirmPasswordValidator = async (_rule: RuleObject, value: string) => {
  if (!value) {
    return Promise.reject(new Error("请确认密码"));
  }
  if (value !== registerForm.userPassword) {
    return Promise.reject(new Error("两次输入的密码不一致"));
  }
  return Promise.resolve();
};

const rules = {
  userAccount: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 4, message: "账号长度不能小于 4 位", trigger: "blur" },
  ],
  userPassword: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于 6 位", trigger: "blur" },
  ],
  checkPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: confirmPasswordValidator, trigger: "blur" },
  ],
};

const handleRegister = async () => {
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    const res = await register(registerForm);
    if (res?.data?.code === 0) {
      message.success("注册成功，请登录");
      router.push("/user/login");
    } else {
      message.error(res?.data?.message ?? "注册失败");
    }
  } catch (e) {
    message.error((e as Error)?.message ?? "网络异常，请稍后重试");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h2 class="register-title">创建账号</h2>
          <p class="register-subtitle">注册到 NoCodeApp</p>
        </div>

        <a-form
          ref="formRef"
          :model="registerForm"
          :rules="rules"
          autocomplete="off"
          @keyup.enter="handleRegister"
        >
          <a-form-item name="userAccount">
            <a-input
              v-model:value="registerForm.userAccount"
              placeholder="请输入账号（至少 4 位）"
              size="large"
              allow-clear
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="userPassword">
            <a-input-password
              v-model:value="registerForm.userPassword"
              placeholder="请输入密码（至少 6 位）"
              size="large"
              allow-clear
            >
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="checkPassword">
            <a-input-password
              v-model:value="registerForm.checkPassword"
              placeholder="请确认密码"
              size="large"
              allow-clear
            >
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              :loading="loading"
              block
              size="large"
              class="submit-btn"
              @click="handleRegister"
            >
              注册
            </a-button>
          </a-form-item>
        </a-form>

        <div class="register-footer">
          已有账号？
          <router-link to="/user/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

.register-container {
  width: 100%;
  max-width: 420px;
}

.register-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.register-subtitle {
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

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.register-footer a {
  color: #1677ff;
}

@media (max-width: 480px) {
  .register-page {
    padding: 16px;
  }

  .register-card {
    padding: 28px 20px;
  }

  .register-title {
    font-size: 24px;
  }
}
</style>
