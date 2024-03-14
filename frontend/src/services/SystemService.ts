import axios from "axios";

export default function useSystemService() {
  const baseUrl = "/api";

  const getPing = (): Promise<number> => {
    return axios
      .get(`${baseUrl}/ping`, { timeout: 5000 }) // set 5 seconds timeout for ping
      .then((response) => response.status);
  };

  return { getPing };
}
