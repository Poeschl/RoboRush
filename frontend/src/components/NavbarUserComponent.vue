<template>
  <div class="navbar-item" v-if="!userStore.loggedIn">
    <button class="button" @click="openRegister">
      <span class="has-text-weight-semibold">Create a account</span>
    </button>
  </div>
  <div class="navbar-item" v-if="!userStore.loggedIn">
    <button class="button is-primary" @click="openLogin">
      <span>Login</span>
    </button>
  </div>
  <div class="navbar-item" v-if="userStore.loggedIn">
    <span class="username">{{ userStore.username }}</span>
  </div>
  <div class="navbar-item" v-if="userStore.loggedIn">
    <button class="button is-text" @click="logout" title="Logout">
      <FontAwesomeIcon icon="fa-solid fa-arrow-right-from-bracket" />
    </button>
  </div>

  <LoginForm v-if="loginIsShowing" :loading="loginLoading" @close="loginIsShowing = false" @login="loginUser" />
  <RegisterForm v-if="registerIsShowing" :loading="registerLoading" @close="registerIsShowing = false" @register="registerUser" />
  <Toast v-if="toast.shown" :type="toast.type" :message="toast.message" @close="() => (toast.shown = false)" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import LoginForm from "@/components/LoginForm.vue";
import type { LoginRequest, RegisterRequest } from "@/models/User";
import RegisterForm from "@/components/RegisterForm.vue";
import { useUserStore } from "@/stores/UserStore";
import Toast from "@/components/Toast.vue";
import { ToastType } from "@/models/ToastType";

const userStore = useUserStore();

const loginIsShowing = ref<boolean>(false);
const loginLoading = ref<boolean>(false);
const registerIsShowing = ref<boolean>(false);
const registerLoading = ref<boolean>(false);
const toast = ref<{ shown: boolean; type: ToastType; message: string }>({ shown: false, type: ToastType.INFO, message: "" });

const openLogin = () => {
  loginLoading.value = false;
  loginIsShowing.value = true;
};

const loginUser = (data: LoginRequest) => {
  loginLoading.value = true;
  userStore
    .login(data)
    .then(() => {
      loginIsShowing.value = false;
      toast.value.message = "Login successful";
      toast.value.type = ToastType.SUCCESS;
      toast.value.shown = true;
    })
    .catch((reason) => {
      loginLoading.value = false;
      toast.value.message = "Login failed";
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
      console.warn(`Login not successful (${reason})`);
    });
};

const openRegister = () => {
  registerLoading.value = false;
  registerIsShowing.value = true;
};

const registerUser = (data: RegisterRequest) => {
  registerLoading.value = true;
  userStore
    .register(data)
    .then(() => {
      registerIsShowing.value = false;
      toast.value.message = "Registered successful. Now login";
      toast.value.type = ToastType.SUCCESS;
      toast.value.shown = true;
    })
    .catch((reason) => {
      registerLoading.value = false;
      toast.value.message = "Registration failed";
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
      console.warn(`Registration not successful (${reason})`);
    });
};

const logout = () => {
  userStore.logout();
  toast.value.message = "You logged out. Bye";
  toast.value.type = ToastType.INFO;
  toast.value.shown = true;
};
</script>

<style scoped>
.navbar-item {
  padding-right: 0.25rem;
  padding-left: 0.25rem;
}

.username {
  cursor: default;
}
</style>
