import type { Position } from "@/models/Map";
import type Color from "@/models/Color";

export interface Robot {
  id: number;
  position: Position;
  fuel: number;
  color: Color;
}

export interface PublicRobot {
  id: number;
  position: Position;
  color: Color;
}
