import type { Position, Size, Tile } from "@/models/Map";
import { ref } from "vue";
import log from "loglevel";

export interface PathNode {
  position: Position;
  globalScore: number;
  heuristicScore: number;
}

export interface VisitedNodeData {
  predecessor: Position | undefined;
  globalScore: number;
}

export function useAStar(mapSize: Size, heightMap: Tile[], start: Position, target: Position, ignoreHeights: boolean) {
  const createMatrixFromHeightmapList = (): Tile[][] => {
    const matrix: Tile[][] = [];
    for (let y = 0; y < mapSize.height; y++) {
      matrix[y] = [];
      for (let x = 0; x < mapSize.width; x++) {
        matrix[y]![x] = heightMap.find((it) => it.position.x == x && it.position.y == y)!;
      }
    }
    return matrix;
  };

  const getNeighbors = (position: Position): Position[] => {
    const neighbors = [];
    // Collect possible neighbors for the given position, exclude everything outside the borders
    if (position.x > 0) neighbors.push({ x: position.x - 1, y: position.y });
    if (position.x < mapSize.width - 1) neighbors.push({ x: position.x + 1, y: position.y });
    if (position.y > 0) neighbors.push({ x: position.x, y: position.y - 1 });
    if (position.y < mapSize.height - 1) neighbors.push({ x: position.x, y: position.y + 1 });
    return neighbors;
  };

  const determineCosts = (from: Position, to: Position): number => {
    if (ignoreHeights) {
      // use 1 as cost if the tile heights should be ignored (shortest way for example)
      return 1;
    } else {
      const fromTile = heightMapMatrix[from.y]![from.x]!;
      const toTile = heightMapMatrix[to.y]![to.x]!;

      const heightDiff = toTile.height - fromTile.height;
      if (heightDiff < 0) {
        // If the difference is negative aka "down the hill" there is only static cost. (1 is used for static cost)
        return 1;
      } else {
        return 1 + heightDiff;
      }
    }
  };

  const heuristicBetweenPoints = (from: Position, to: Position): number => {
    // Get an estimate how far the "from" position is from the "to" position
    return Math.sqrt(Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2));
  };

  const reconstructPath = (current: Position): Position[] => {
    let currentPosition = current;
    const path = [currentPosition];

    // Go over all predecessors and fill an array from the front to get the whole path
    let reachedStart = false;
    while (!reachedStart) {
      const predecessor = visitedNodes.get(`${currentPosition.x},${currentPosition.y}`)!.predecessor;
      if (predecessor == undefined) {
        // If no predecessor is given, the start is reached
        reachedStart = true;
        break;
      }
      currentPosition = predecessor;
      path.unshift(currentPosition);
    }
    return path;
  };

  // List of all not-visited positions
  let openNodes: PathNode[] = [{ position: start, globalScore: 0, heuristicScore: heuristicBetweenPoints(start, target) }];
  // List for the costs to reach a position from the start point n the predecessor of a position. Pre-populated with the start
  // The key is the x and y position of a position
  const visitedNodes: Map<string, VisitedNodeData> = new Map([[`${start.x},${start.y}`, { predecessor: undefined, globalScore: 0 }]]);
  // The provided heightmap as a matrix for faster access times
  const heightMapMatrix = createMatrixFromHeightmapList();

  const findPath = (): Position[] | null => {
    while (openNodes.length > 0) {
      // Current position is the one with the shortest estimated distance to the target
      const current = openNodes.reduce((a, b) => (b.heuristicScore < a.heuristicScore ? b : a));
      // Check if the target is reached
      if (current.position.x === target.x && current.position.y === target.y) {
        log.info("Found a path from ", start, " to ", target);
        return reconstructPath(current.position);
      }

      // Remove current position from the list of not visited (open) positions
      openNodes = openNodes.filter((node) => node.position !== current.position);

      // For each of the current's position neighbor
      for (const neighbor of getNeighbors(current.position)) {
        // Calculate costs to reach the neighbor position from the start
        const neighborsGlobalScore = visitedNodes.get(`${current.position.x},${current.position.y}`)!.globalScore + determineCosts(current.position, neighbor);

        // Use Infinity if the position was never visited.
        let knownCostForNeighbor = visitedNodes.get(`${neighbor.x},${neighbor.y}`)?.globalScore;
        if (knownCostForNeighbor == undefined) {
          knownCostForNeighbor = Infinity;
        }

        // Compare if the calculated cost is less than the known cost for the neighbor.
        if (neighborsGlobalScore < knownCostForNeighbor) {
          // If less, save the neighbor as new shortest path with preprocessor and costs
          visitedNodes.set(`${neighbor.x},${neighbor.y}`, { predecessor: current.position, globalScore: neighborsGlobalScore });
          // And add it to the list of open position which should be visited in the future
          const estimationCostToTarget = neighborsGlobalScore + heuristicBetweenPoints(neighbor, target);
          openNodes.push({ position: neighbor, globalScore: neighborsGlobalScore, heuristicScore: estimationCostToTarget });
        }
      }
    }
    return null;
  };

  return { findPath };
}
