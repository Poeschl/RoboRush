import axios from "axios";
import type { ActiveRobot, Move, PublicRobot, Scan, ScoreboardEntry } from "@/models/Robot";
import { correctTypesFromJson } from "@/models/Robot";
import Color from "@/models/Color";
import axiosWithAuth from "@/config/axiosWithAuth";
import type { Position } from "@/models/Map";

export default function useRobotService() {
  const baseRobotUrl = "/api/robot";

  const getRobots = (): Promise<PublicRobot[]> => {
    return axios.get(`${baseRobotUrl}/all/active`).then((response) =>
      response.data.map((origin: PublicRobot) => {
        return { id: origin.id, name: origin.name, position: origin.position, color: new Color(origin.color.r, origin.color.g, origin.color.b) } as PublicRobot;
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

  const getAllRobotsKnownPositions = (): Promise<Position[]> => {
    return axios.get(`${baseRobotUrl}/all/knownPositions`).then((response) => response.data);
  };

  const getUserRobotKnownPositions = (): Promise<Position[]> => {
    return axiosWithAuth.get(`${baseRobotUrl}/knownPositions`).then((response) => response.data);
  };

  const registerCurrentRobotForGame = (): Promise<void> => {
    return axiosWithAuth.post(`${baseRobotUrl}/attend`);
  };

  const moveRobot = (direction: string): Promise<void> => {
    return axiosWithAuth.post(`${baseRobotUrl}/action/move`, { direction: direction } as Move);
  };

  const scanOnRobot = (distance: number): Promise<void> => {
    return axiosWithAuth.post(`${baseRobotUrl}/action/scan`, { distance: distance } as Scan);
  };

  const waitOnRobot = (): Promise<void> => {
    return axiosWithAuth.post(`${baseRobotUrl}/action/wait`);
  };

  const refuelRobot = (): Promise<void> => {
    return axiosWithAuth.post(`${baseRobotUrl}/action/refuel`);
  };

  const solarChargeRobot = (): Promise<void> => {
    return axiosWithAuth.post(`${baseRobotUrl}/action/solarCharge`);
  };

  const getTop10Robots = (): Promise<ScoreboardEntry[]> => {
    return axios.get(`${baseRobotUrl}/all/scores`).then((response) =>
      response.data.map((origin: ScoreboardEntry) => {
        return { name: origin.name, color: new Color(origin.color.r, origin.color.g, origin.color.b), score: origin.score } as ScoreboardEntry;
      }),
    );
  };

  return {
    getRobots,
    getUserRobot,
    registerCurrentRobotForGame,
    moveRobot,
    scanOnRobot,
    waitOnRobot,
    refuelRobot,
    solarChargeRobot,
    getTop10Robots,
    getAllRobotsKnownPositions,
    getUserRobotKnownPositions,
  };
}
