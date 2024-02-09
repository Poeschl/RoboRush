import axios from "axios";
import type { ActiveRobot, Move, PublicRobot, Scan } from "@/models/Robot";
import Color from "@/models/Color";
import axiosWithAuth from "@/config/axiosWithAuth";
import { correctTypesFromJson } from "@/models/Robot";

export default class RobotService {
  private baseRobotUrl = "/api/robot";

  getRobots(): Promise<PublicRobot[]> {
    return axios.get(`${this.baseRobotUrl}/all`).then((response) =>
      response.data.map((origin: PublicRobot) => {
        return { id: origin.id, position: origin.position, color: new Color(origin.color.r, origin.color.g, origin.color.b) } as PublicRobot;
      }),
    );
  }

  getUserRobot(): Promise<ActiveRobot | undefined> {
    return axiosWithAuth.get(`${this.baseRobotUrl}`).then((response) => {
      const userRobot: ActiveRobot = response.data;
      if (userRobot != null) {
        return correctTypesFromJson(userRobot);
      } else {
        return undefined;
      }
    });
  }
}
