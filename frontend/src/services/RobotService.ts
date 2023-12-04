import axios from "axios";
import type { PublicRobot } from "@/models/Robot";
import Color from "@/models/Color";

export default class RobotService {
  private baseMapUrl = "/rest/robot";

  getAllRobotStates(): Promise<PublicRobot[]> {
    return axios.get(`${this.baseMapUrl}/all`).then((response) =>
      response.data.map((origin: PublicRobot) => {
        const randomR = Math.round(Math.random() * 255);
        const randomG = 0; //Math.round(Math.random() * 255)
        const randomB = Math.round(Math.random() * 255);
        return { id: origin.id, position: origin.position, color: new Color(randomR, randomG, randomB) } as PublicRobot;
      }),
    );
  }
}
