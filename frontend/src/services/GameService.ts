import axios from "axios";
import type { Game } from "@/models/Game";
import { GameState } from "@/models/Game";

export function useGameInfo() {
  const baseGameUrl = "/api/game";

  const getCurrentGame = (): Promise<Game> => {
    return axios.get(`${baseGameUrl}`).then((response) => response.data);
  };

  return { getCurrentGame };
}
