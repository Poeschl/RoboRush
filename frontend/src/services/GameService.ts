import axios from "axios";
import type { Game } from "@/models/Game";

export function useGameService() {
  const baseGameUrl = "/api/game";

  const getCurrentGame = (): Promise<Game> => {
    return axios.get(`${baseGameUrl}`).then((response) => response.data);
  };

  return { getCurrentGame };
}
