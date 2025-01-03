import type { LoginRequest, LoginResponse, RegisterRequest } from "@/models/User";
import axios from "axios";
import type { AxiosResponse } from "axios";

export default function useAuthService() {
  const baseAuthUrl = "/api/auth";

  const register = (input: RegisterRequest): Promise<boolean> => {
    //Strip the repeated password before the sending
    return axios
      .post(`${baseAuthUrl}/register`, { username: input.username, password: input.password } as LoginRequest)
      .then((response) => response.status == 200 || response.status == 204);
  };

  const login = (input: LoginRequest): Promise<LoginResponse> => {
    return axios.post(`${baseAuthUrl}/login`, input).then((response: AxiosResponse<LoginResponse>) => response.data);
  };

  return { register, login };
}
