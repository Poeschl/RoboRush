import type { Path, PlaygroundMap, Size, Tile } from "@/models/Map";
import { useAStar } from "@/services/PathFindingService";
import { onMounted } from "vue";
import log from "loglevel";

export interface PathFindingWorkerInput {
  mapSize: Size;
  heightMap: Tile[];
  inputPath: Path;
  ignoreHeights: boolean;
}

function executeAstarOnPath(input: PathFindingWorkerInput): Path {
  const calculatedPath: Path = { points: [] };

  // Calculate the paths between all path points
  for (let index = 1; index < input.inputPath.points.length; index++) {
    const start = input.inputPath.points[index - 1];
    const target = input.inputPath.points[index];
    const astar = useAStar(input.mapSize, input.heightMap, start, target, input.ignoreHeights);
    const partlyPath = astar.findPath();

    if (partlyPath) {
      // If a path is found
      calculatedPath.points = calculatedPath.points.concat(partlyPath);
    }
  }
  return calculatedPath;
}

onmessage = (message) => {
  const data: PathFindingWorkerInput = JSON.parse(message.data);
  const result = executeAstarOnPath(data);
  // Convert data to json, since couldn't get the object ot work
  postMessage(JSON.stringify(result));
};
