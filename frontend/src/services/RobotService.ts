import axios from "axios";
import type { PublicRobot } from "@/models/Robot";
import Color from "@/models/Color";

export default class RobotService {
  private baseMapUrl = "/api/robot";

  getAllRobotStates(): Promise<PublicRobot[]> {
    return axios.get(`${this.baseMapUrl}/all`).then((response) =>
      response.data.map((origin: PublicRobot) => {
        return { id: origin.id, position: origin.position, color: new Color(origin.color.r, origin.color.g, origin.color.b) } as PublicRobot;
      }),
    );
  }
}
