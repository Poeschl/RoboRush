import type { LoginRequest, LoginResponse, RegisterRequest, User } from "@/models/User";
import axios from "axios";
import type { AxiosResponse } from "axios";

export default class AuthService {
  private baseAuthUrl = "/api/auth";

  register(input: RegisterRequest): Promise<boolean> {
    //Strip the repeated password before the sending
    return axios
      .post(`${this.baseAuthUrl}/register`, { username: input.username, password: input.password } as LoginRequest)
      .then((response) => response.status == 200 || response.status == 204);
  }

  login(input: LoginRequest): Promise<LoginResponse> {
    return axios.post(`${this.baseAuthUrl}/login`, input).then((response: AxiosResponse<LoginResponse>) => response.data);
  }
}
