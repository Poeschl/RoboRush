<template>
  <div class="navbar-item is-button" v-if="!userStore.loggedIn">
    <button class="button" @click="openRegister" :disabled="!registerEnabled">
      <span class="has-text-weight-semibold">Create a account</span>
    </button>
  </div>
  <div class="navbar-item is-button" v-if="!userStore.loggedIn">
    <button class="button is-primary" @click="openLogin">
      <span>Login</span>
    </button>
  </div>
  <a class="navbar-item" v-if="userStore.loggedIn" @click="() => (userDetailsShown = true)">
    <FontAwesomeIcon icon="fa-regular fa-user" class="mr-2" />
    <span class="username">{{ userStore.username }}</span>
  </a>
  <a class="navbar-item" v-if="userStore.loggedIn" title="Logout" @click="logout">
    <FontAwesomeIcon icon="fa-solid fa-arrow-right-from-bracket" />
  </a>

  <LoginForm v-if="loginIsShowing" :loading="loginLoading" @close="loginIsShowing = false" @login="loginUser" />
  <RegisterForm v-if="registerIsShowing && registerEnabled" :loading="registerLoading" @close="registerIsShowing = false" @register="registerUser" />
  <UserModal v-if="userDetailsShown" @close="() => (userDetailsShown = false)" />
  <Toast v-if="toast.shown" :type="toast.type" :message="toast.message" @close="() => (toast.shown = false)" />
</template>

<script setup lang="ts">
import { computed, inject, ref } from "vue";
import LoginForm from "@/components/LoginForm.vue";
import type { LoginRequest, RegisterRequest } from "@/models/User";
import RegisterForm from "@/components/RegisterForm.vue";
import { useUserStore } from "@/stores/UserStore";
import Toast from "@/components/Toast.vue";
import { ToastType } from "@/models/ToastType";
import UserModal from "@/components/UserModal.vue";
import log from "loglevel";
import { useConfigStore } from "@/stores/ConfigStore";
import type { PlausibleInitOptions } from "plausible-tracker/build/main/lib/tracker";

const userStore = useUserStore();
const configStore = useConfigStore();
const plausible = inject<{ trackEvent: (key: string, props: {}) => {} }>("plausible");

const loginIsShowing = ref<boolean>(false);
const loginLoading = ref<boolean>(false);
const registerIsShowing = ref<boolean>(false);
const registerLoading = ref<boolean>(false);
const registerEnabled = computed<boolean>(() => configStore.clientSettings.enableUserRegistration);
const toast = ref<{ shown: boolean; type: ToastType; message: string }>({ shown: false, type: ToastType.INFO, message: "" });
const userDetailsShown = ref<boolean>(false);

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
      plausible?.trackEvent("User logged in", {});
    })
    .catch((reason) => {
      loginLoading.value = false;
      toast.value.message = "Login failed";
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
      log.warn(`Login not successful (${reason})`);
    });
};

const openRegister = () => {
  if (registerEnabled.value) {
    registerLoading.value = false;
    registerIsShowing.value = true;
  }
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
      plausible?.trackEvent("User registered", {});
    })
    .catch((reason) => {
      registerLoading.value = false;
      toast.value.message = "Registration failed";
      toast.value.type = ToastType.ERROR;
      toast.value.shown = true;
      log.warn(`Registration not successful (${reason})`);
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
.navbar-item.is-button {
  padding-right: 0.25rem;
  padding-left: 0.25rem;
}
</style>
