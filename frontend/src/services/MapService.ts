import axios from "axios";
import type { Tile } from "@/models/Map";

export default class MapService {
  private baseMapUrl = "/rest/map";

  getHeightMap(): Promise<Tile[]> {
    return axios.get(`${this.baseMapUrl}/heights`).then((response) => response.data);
  }
}
