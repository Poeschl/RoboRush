import axios from "axios";
import type { AxiosInstance } from "axios";
import { useUserStore } from "@/stores/UserStore";
import type { User } from "@/models/User";

export class AxiosWithAuth {
  private axios = axios.create();

  constructor() {
    this.setUpAuth();
  }

  setUpAuth() {
    axios.interceptors.request.use((config) => {
      const userStore = useUserStore();

      if (userStore.loggedIn) {
        const user: User = userStore.user!;
        config.headers["Authorization"] = `Bearer ${user.accessToken}`;
      }

      return config;
    });
  }

  getAxios(): AxiosInstance {
    return axios;
  }
}

export default new AxiosWithAuth().getAxios();
