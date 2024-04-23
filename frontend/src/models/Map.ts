export interface PlaygroundMap {
  id: number;
  mapName: string;
  size: Size;
  possibleStartPositions: Position[];
  targetPosition: Position;
  active: boolean;
  maxRobotFuel: number;
  solarChargeEnabled: boolean;
  mapData: Tile[];
}

export interface PlaygroundMapAttributes {
  id: number;
  mapName: string;
  maxRobotFuel: number;
  solarChargeEnabled: boolean;
}

export interface Size {
  width: number;
  height: number;
}

export interface Tile {
  position: Position;
  height: number;
  type: TileType;
}

export interface Position {
  x: number;
  y: number;
}

export enum TileType {
  DEFAULT_TILE = "DEFAULT_TILE",
  START_TILE = "START_TILE",
  TARGET_TILE = "TARGET_TILE",
  FUEL_TILE = "FUEL_TILE",
}
