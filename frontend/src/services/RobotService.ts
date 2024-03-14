import axios from "axios";
import type { ActiveRobot, PublicRobot } from "@/models/Robot";
import Color from "@/models/Color";
import axiosWithAuth from "@/config/axiosWithAuth";
import { correctTypesFromJson } from "@/models/Robot";

export default function useRobotService() {
  const baseRobotUrl = "/api/robot";

  const getRobots = (): Promise<PublicRobot[]> => {
    return axios.get(`${baseRobotUrl}/all`).then((response) =>
      response.data.map((origin: PublicRobot) => {
        return { id: origin.id, position: origin.position, color: new Color(origin.color.r, origin.color.g, origin.color.b) } as PublicRobot;
      }),
    );
  };

  const getUserRobot = (): Promise<ActiveRobot | undefined> => {
    return axiosWithAuth.get(`${baseRobotUrl}`).then((response) => {
      const userRobot: ActiveRobot = response.data;
      if (userRobot != null) {
        return correctTypesFromJson(userRobot);
      } else {
        return undefined;
      }
    });
  };

  return { getRobots, getUserRobot };
}
