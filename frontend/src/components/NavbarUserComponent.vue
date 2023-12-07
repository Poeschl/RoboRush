<template>
  <div class="navbar-item">
    <button class="button" v-if="!userIsLoggedIn">
      <span class="has-text-weight-semibold">Create a account</span>
    </button>
  </div>
  <div class="navbar-item" v-if="!userIsLoggedIn">
    <button class="button is-primary" @click="openLogin">
      <span>Login</span>
    </button>
  </div>

  <LoginForm v-if="loginIsShowing" :loading="loginLoading" @close="loginIsShowing = false" @login="loginUser" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import LoginForm from "@/components/LoginForm.vue";
import type { LoginRequest } from "@/models/User";

const userIsLoggedIn = ref<boolean>(false);
const loginIsShowing = ref<boolean>(false);
const loginLoading = ref<boolean>(false);

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
</script>

<style scoped>
.navbar-item {
  padding-right: 0.25rem;
  padding-left: 0.25rem;
}
</style>
