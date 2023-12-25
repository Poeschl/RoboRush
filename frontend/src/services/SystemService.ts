import axios from "axios";

export default class SystemService {
  private baseUrl = "/api";

  getPing(): Promise<number> {
    return axios
      .get(`${this.baseUrl}/ping`, { timeout: 5000 }) // set 5 seconds timeout for ping
      .then((response) => response.status);
  }
}
