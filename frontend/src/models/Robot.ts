import type { Position } from "@/models/Map";
import Color from "@/models/Color";
import { createRenderer } from "vue";

export interface PublicRobot {
  id: number;
  position: Position;
  color: Color;
}

export interface Move {
  direction: string;
}

export interface Scan {
  distance: string;
}

export interface ActiveRobot {
  id: number;
  color: Color;
  fuel: number;
  position: Position;
  nextAction: Move | Scan | undefined;
  lastResult: string | undefined;
}

export function correctTypesFromJson(activeBot: ActiveRobot): ActiveRobot {
  const correctRobot = activeBot;
  correctRobot.color = new Color(correctRobot.color.r, correctRobot.color.g, correctRobot.color.b);

  if (correctRobot.nextAction != null && "distance" in correctRobot.nextAction) {
    correctRobot.nextAction = correctRobot.nextAction as Scan;
  } else if (correctRobot.nextAction != null && "direction" in correctRobot.nextAction) {
    correctRobot.nextAction = correctRobot.nextAction as Move;
  } else {
    correctRobot.nextAction = undefined;
  }

  if (correctRobot.lastResult == null) {
    correctRobot.lastResult = undefined;
  }

  return correctRobot;
}
