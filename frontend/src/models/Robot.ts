import type { Position } from "@/models/Map";
import Color from "@/models/Color";

export interface PublicRobot {
  id: number;
  position: Position;
  color: Color;
}

export interface Action {
  type: string;
}

export interface Move extends Action {
  direction: string;
}

export interface Scan extends Action {
  distance: number;
}

export interface Wait extends Action {}

export interface Refuel extends Action {}

export interface SolarCharge extends Action {}

export interface ActiveRobot {
  id: number;
  color: Color;
  fuel: number;
  maxFuel: number;
  position: Position;
  nextAction: Action | undefined;
  lastResult: string | undefined;
}

export function correctTypesFromJson(activeBot: ActiveRobot): ActiveRobot {
  const correctRobot = activeBot;
  correctRobot.color = new Color(correctRobot.color.r, correctRobot.color.g, correctRobot.color.b);

  if (correctRobot.nextAction != null && correctRobot.nextAction.type == "scan") {
    correctRobot.nextAction = correctRobot.nextAction as Scan;
  } else if (correctRobot.nextAction != null && correctRobot.nextAction.type == "move") {
    correctRobot.nextAction = correctRobot.nextAction as Move;
  } else if (correctRobot.nextAction != null && correctRobot.nextAction.type == "wait") {
    correctRobot.nextAction = correctRobot.nextAction as Wait;
  } else if (correctRobot.nextAction != null && correctRobot.nextAction.type == "refuel") {
    correctRobot.nextAction = correctRobot.nextAction as Refuel;
  } else if (correctRobot.nextAction != null && correctRobot.nextAction.type == "solarCharge") {
    correctRobot.nextAction = correctRobot.nextAction as SolarCharge;
  } else {
    correctRobot.nextAction = undefined;
  }

  if (correctRobot.lastResult == null) {
    correctRobot.lastResult = undefined;
  }

  return correctRobot;
}
