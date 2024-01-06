import axios from "axios";
import type { Tile } from "@/models/Map";
import type { PublicRobot } from "@/models/Robot";
import Color from "@/models/Color";

export default class MapService {
  private baseMapUrl = "/api/map";

  getHeightMap(): Promise<Tile[]> {
    return axios.get(`${this.baseMapUrl}/heights`).then((response) => response.data);
  }
}
