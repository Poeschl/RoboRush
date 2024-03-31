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
}
