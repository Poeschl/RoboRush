export interface Tile {
  position: Position;
  height: number;
}

export interface Position {
  x: number;
  y: number;
}

export const createDummyMap = (maxX: number, maxY: number): HeightMap => {
  const tiles: Tile[] = [];
  for (let x = 0; x <= maxX; x++) {
    for (let y = 0; y <= maxY; y++) {
      tiles.push({ position: { x: x, y: y }, height: Math.floor(Math.random() * 8) });
    }
  }
  return { tiles: tiles };
};
