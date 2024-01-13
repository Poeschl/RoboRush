import { defineStore } from "pinia";
import { computed, ref } from "vue";
import AuthService from "@/services/AuthService";
import type { LoginRequest, RegisterRequest, User } from "@/models/User";

const authService = new AuthService();

export const useUserStore = defineStore(
  "userStore",
  () => {
    const user = ref<User | undefined>(undefined);

    const loggedIn = computed<boolean>(() => user.value != undefined);
    const username = computed<string | undefined>(() => user.value?.username);
    const accessToken = computed<string | undefined>(() => user.value?.accessToken);

    function login(loginRequest: LoginRequest): Promise<void> {
      return authService.login(loginRequest).then((response) => {
        user.value = { username: loginRequest.username, accessToken: response.accessToken };
      });
    }

    function register(registerRequest: RegisterRequest): Promise<boolean> {
      return authService.register(registerRequest);
    }

    function logout() {
      user.value = undefined;
    }

    return { user, loggedIn, username, accessToken, login, register, logout };
  },
  {
    persist: {
      storage: localStorage,
    },
  },
);
