<template>
  <div class="navbar-item">
    <button class="button" v-if="!userIsLoggedIn" @click="openRegister">
      <span class="has-text-weight-semibold">Create a account</span>
    </button>
  </div>
  <div class="navbar-item" v-if="!userIsLoggedIn">
    <button class="button is-primary" @click="openLogin">
      <span>Login</span>
    </button>
  </div>

  <LoginForm v-if="loginIsShowing" :loading="loginLoading" @close="loginIsShowing = false" @login="loginUser" />
  <RegisterForm v-if="registerIsShowing" :loading="registerLoading" @close="registerIsShowing = false" @register="registerUser" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import LoginForm from "@/components/LoginForm.vue";
import type { LoginRequest, RegisterRequest } from "@/models/User";
import RegisterForm from "@/components/RegisterForm.vue";

const userIsLoggedIn = ref<boolean>(false);
const loginIsShowing = ref<boolean>(false);
const loginLoading = ref<boolean>(false);
const registerIsShowing = ref<boolean>(false);
const registerLoading = ref<boolean>(false);

const openLogin = () => {
  loginLoading.value = false;
  loginIsShowing.value = true;
};

const loginUser = (data: LoginRequest) => {
  loginLoading.value = true;
  console.info(JSON.stringify(data));
  //TODO: Call login in user Store and replace dummy loading time
  setTimeout(() => (loginIsShowing.value = false), 1000);
};

const openRegister = () => {
  registerLoading.value = false;
  registerIsShowing.value = true;
};

const registerUser = (data: RegisterRequest) => {
  registerLoading.value = true;
  console.info(JSON.stringify(data));
  //TODO: Call register in user Store and replace dummy loading time
  setTimeout(() => (registerIsShowing.value = false), 3000);
};
</script>

<style scoped>
.navbar-item {
  padding-right: 0.25rem;
  padding-left: 0.25rem;
}
</style>
