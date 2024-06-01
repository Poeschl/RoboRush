import axios from "axios";
import type { Game } from "@/models/Game";
import type { PlaygroundMap } from "@/models/Map";

export function useGameService() {
  const baseGameUrl = "/api/game";

  const getCurrentGame = (): Promise<Game> => {
    return axios.get(`${baseGameUrl}`).then((response) => response.data);
  };

  const getMap = (): Promise<PlaygroundMap> => {
    return axios.get(`${baseGameUrl}/map`).then((response) => response.data);
  };

  return { getCurrentGame, getMap };
}
