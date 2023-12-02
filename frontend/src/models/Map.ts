export interface HeightMap {
  tiles: Tile[];
}

export interface Tile {
  position: Position;
  height: number;
}

export interface Position {
  x: number;
  y: number;
}

export const createDummyMap = (): HeightMap => {
  const tiles: Tile[] = [];
  for (let x = 0; x < 12; x++) {
    for (let y = 0; y < 10; y++) {
      tiles.push({ position: { x: x, y: y }, height: Math.floor(Math.random() * 8) });
    }
  }
  return { tiles: tiles };
};
