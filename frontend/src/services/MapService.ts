import axios from "axios";
import type { Tile } from "@/models/Map";

export default function useMapService() {
  const baseMapUrl = "/api/map";

  const getHeightMap = (): Promise<Tile[]> => {
    return axios.get(`${baseMapUrl}/heights`).then((response) => response.data);
  };

  return { getHeightMap };
}
