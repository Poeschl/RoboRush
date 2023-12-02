import type { Position } from "@/models/Map";

export interface Robot {
  id: number;
  position: Position;
  fuel: number;
}

export interface PublicRobot {
  id: number;
  position: Position;
}
